package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Codebreakable;
import net.geforcemods.securitycraft.blockentities.KeycardLockBlockEntity;
import net.geforcemods.securitycraft.items.KeycardHolderItem;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.items.UniversalKeyChangerItem;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin wall/floor/ceiling panel form of the Keycard Reader - same link/use logic as {@link KeycardReaderBlock}. */
public class KeycardLockBlock extends AbstractPanelBlock {
	public static final VoxelShape FLOOR_NS = Block.box(2.0D, 0.0D, 1.0D, 14.0D, 1.0D, 15.0D);
	public static final VoxelShape FLOOR_EW = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 1.0D, 14.0D);
	public static final VoxelShape CEILING_NS = Block.box(2.0D, 15.0D, 1.0D, 14.0D, 16.0D, 15.0D);
	public static final VoxelShape CEILING_EW = Block.box(1.0D, 15.0D, 2.0D, 15.0D, 16.0D, 14.0D);
	public static final VoxelShape WALL_N = Block.box(2.0D, 1.0D, 15.0D, 14.0D, 15.0D, 16.0D);
	public static final VoxelShape WALL_E = Block.box(0.0D, 1.0D, 2.0D, 1.0D, 15.0D, 14.0D);
	public static final VoxelShape WALL_S = Block.box(2.0D, 1.0D, 0.0D, 14.0D, 15.0D, 1.0D);
	public static final VoxelShape WALL_W = Block.box(15.0D, 1.0D, 2.0D, 16.0D, 15.0D, 14.0D);

	public KeycardLockBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeycardLockBlockEntity be))
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		ItemStack stack = player.getItemInHand(hand);
		Item item = stack.getItem();

		if (!be.isSetUp() && !(item instanceof UniversalKeyChangerItem)) {
			if (be.isOwnedBy(player) || be.isAllowed(player)) {
				if (player instanceof ServerPlayer)
					player.openMenu(be);
			}

			return InteractionResult.SUCCESS;
		}

		if (item instanceof UniversalKeyChangerItem) {
			if (be.isOwnedBy(player)) {
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				be.reset();
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:keycard_lock.reset"), ChatFormatting.GREEN);
			}
			else
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", be.getOwner().getName()), ChatFormatting.RED);

			return InteractionResult.SUCCESS;
		}

		if (be.isDenied(player)) {
			if (be.sendsDenylistMessage())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);

			return InteractionResult.SUCCESS;
		}

		boolean isCodebreaker = item == SCContent.CODEBREAKER;
		boolean isHolder = item == SCContent.KEYCARD_HOLDER;

		if (!isHolder && !isCodebreaker && (!(item instanceof KeycardItem) || !KeycardItem.isLinked(stack))) {
			if (item instanceof KeycardItem)
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:keycard_lock.unlinked_keycard"), ChatFormatting.RED);

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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACE)) {
			case FLOOR -> switch (state.getValue(FACING)) {
				case NORTH, SOUTH -> FLOOR_NS;
				case EAST, WEST -> FLOOR_EW;
				default -> Shapes.empty();
			};
			case CEILING -> switch (state.getValue(FACING)) {
				case NORTH, SOUTH -> CEILING_NS;
				case EAST, WEST -> CEILING_EW;
				default -> Shapes.empty();
			};
			case WALL -> switch (state.getValue(FACING)) {
				case NORTH -> WALL_N;
				case EAST -> WALL_E;
				case SOUTH -> WALL_S;
				case WEST -> WALL_W;
				default -> Shapes.empty();
			};
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeycardLockBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		return level.isClientSide ? null : net.geforcemods.securitycraft.util.LevelUtils.createTickerHelper(type, net.geforcemods.securitycraft.SCContent.KEYCARD_LOCK_BLOCK_ENTITY, net.geforcemods.securitycraft.util.LevelUtils::blockEntityTicker);
	}
}
