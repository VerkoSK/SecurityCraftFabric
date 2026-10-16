package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Codebreakable;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.items.KeycardHolderItem;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A programmable card reader: link a keycard to it (owner only), then anyone the card allows can use it to pulse
 * redstone. A Codebreaker can hack it directly; a Keycard Holder tries every card inside it. Like every reinforced
 * block it can only be mined by whoever placed it.
 */
public class KeycardReaderBlock extends DisguisableBlock implements net.geforcemods.securitycraft.api.IDoorActivator {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final DirectionProperty ROTATION = DirectionProperty.create("rotation", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public KeycardReaderBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ROTATION, Direction.NORTH).setValue(POWERED, false).setValue(WATERLOGGED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeycardReaderBlockEntity be))
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (be.isDisabled()) {
			player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			return InteractionResult.SUCCESS;
		}

		if (be.isDenied(player)) {
			if (be.sendsDenylistMessage())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);

			return InteractionResult.SUCCESS;
		}

		ItemStack stack = player.getItemInHand(hand);
		Item item = stack.getItem();
		boolean isCodebreaker = item == SCContent.CODEBREAKER;
		boolean isHolder = item == SCContent.KEYCARD_HOLDER;

		if (!isHolder && !isCodebreaker && (!(item instanceof KeycardItem) || !KeycardItem.isLinked(stack))) {
			//no usable item in hand: owner opens the programming screen, everyone else is told to bring a keycard
			if (be.isOwnedBy(player) || be.isAllowed(player)) {
				if (player instanceof ServerPlayer)
					player.openMenu(be);
			}

			return InteractionResult.SUCCESS;
		}

		if (isCodebreaker) {
			be.handleCodebreaking(player, hand);
			return InteractionResult.SUCCESS;
		}

		MutableComponent feedback = isHolder ? be.insertHolder(stack, player) : be.insertCard(stack, player);

		if (feedback != null)
			PlayerUtils.sendMessageToPlayer(player, Component.translatable(getDescriptionId()), feedback, ChatFormatting.RED);

		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && state.getValue(POWERED)) {
			level.updateNeighborsAt(pos, this);
			BlockUtils.updateIndirectNeighbors(level, pos, this);
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction facing = ctx.getNearestLookingDirection().getOpposite();
		BlockState state = super.getStateForPlacement(ctx).setValue(FACING, facing).setValue(POWERED, false);

		if (facing == Direction.UP || facing == Direction.DOWN)
			return state.setValue(ROTATION, ctx.getHorizontalDirection().getOpposite());
		else
			return state.setValue(ROTATION, facing);
	}

	@Override
	public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(ROTATION, rot.rotate(state.getValue(ROTATION)));
	}

	@Override
	public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ROTATION, POWERED, WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeycardReaderBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		return level.isClientSide ? null : net.geforcemods.securitycraft.util.LevelUtils.createTickerHelper(type, net.geforcemods.securitycraft.SCContent.KEYCARD_READER_BLOCK_ENTITY, net.geforcemods.securitycraft.util.LevelUtils::blockEntityTicker);
	}
}
