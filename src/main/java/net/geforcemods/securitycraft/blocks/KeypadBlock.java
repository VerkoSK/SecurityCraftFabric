package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The keypad: an ownable, passcode-protected block. First player to use it becomes the owner and
 * sets a code; afterwards anyone may attempt the code, and a correct attempt emits a redstone
 * pulse. The owner can reprogram it by sneak-using. Faithful core of the upstream KeypadBlock,
 * minus disguise/module systems (roadmapped).
 */
public class KeypadBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public KeypadBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(POWERED))
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (!(level.getBlockEntity(pos) instanceof KeypadBlockEntity be) || !(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.PASS;

		Owner owner = be.getOwner();

		if (owner.isOwner(player) && player.isShiftKeyDown())
			// Owner reprograms the code.
			NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
		else if (!be.hasPasscode()) {
			if (owner.isOwner(player))
				NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
			else
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcodeProtected.notSetUp"), true);
		}
		else
			NetworkHandler.openKeypadScreen(serverPlayer, pos, false, owner.getName());

		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);

		if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof KeypadBlockEntity be)
			be.setOwner(player.getName().getString(), player.getUUID().toString());
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		// End of the redstone pulse.
		if (state.getValue(POWERED)) {
			level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
			net.geforcemods.securitycraft.util.BlockUtils.updateIndirectNeighbors(level, pos, this);
		}
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && state.getValue(POWERED))
			net.geforcemods.securitycraft.util.BlockUtils.updateIndirectNeighbors(level, pos, this);

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return getSignal(state, level, pos, direction);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadBlockEntity(pos, state);
	}
}
