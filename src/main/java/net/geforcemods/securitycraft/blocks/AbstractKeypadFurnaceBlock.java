package net.geforcemods.securitycraft.blocks;

import java.util.stream.Stream;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.AbstractKeypadFurnaceBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Common base for the keypad furnace family (furnace, smoker, blast furnace): a vanilla furnace that opens like
 * a door and is guarded by a salted passcode, exactly like a keypad chest.
 *
 * <p>Ported from upstream's {@code AbstractKeypadFurnaceBlock}, which extends {@code DisguisableBlock} (itself
 * extending {@code OwnableBlock}). This port has no {@code DisguisableBlock}/{@code IDisguisable} equivalent, so
 * the shape/light/sound disguise overrides are dropped (disguise module support is kept at render-only level in
 * {@link AbstractKeypadFurnaceBlockEntity}, same as {@code KeypadBlockEntity}); the ownership behaviour
 * (owner-only breaking, owner set on placement) is kept by extending {@link OwnableBlock} directly instead.
 */
public abstract class AbstractKeypadFurnaceBlock extends OwnableBlock implements SimpleWaterloggedBlock {
	public static final EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape NORTH_OPEN = Stream.of(Block.box(11, 1, 1, 12, 2, 2), Block.box(0, 0, 2, 16, 16, 16), Block.box(4, 1, 0, 12, 2, 1), Block.box(4, 1, 1, 5, 2, 2)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape NORTH_CLOSED = Stream.of(Block.box(4, 14, 1, 5, 15, 2), Block.box(11, 14, 1, 12, 15, 2), Block.box(0, 0, 2, 16, 16, 16), Block.box(4, 14, 0, 12, 15, 1)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape EAST_OPEN = Stream.of(Block.box(14, 1, 11, 15, 2, 12), Block.box(0, 0, 0, 14, 16, 16), Block.box(15, 1, 4, 16, 2, 12), Block.box(14, 1, 4, 15, 2, 5)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape EAST_CLOSED = Stream.of(Block.box(14, 14, 4, 15, 15, 5), Block.box(14, 14, 11, 15, 15, 12), Block.box(0, 0, 0, 14, 16, 16), Block.box(15, 14, 4, 16, 15, 12)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape SOUTH_OPEN = Stream.of(Block.box(4, 1, 14, 5, 2, 15), Block.box(0, 0, 0, 16, 16, 14), Block.box(4, 1, 15, 12, 2, 16), Block.box(11, 1, 14, 12, 2, 15)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape SOUTH_CLOSED = Stream.of(Block.box(11, 14, 14, 12, 15, 15), Block.box(4, 14, 14, 5, 15, 15), Block.box(0, 0, 0, 16, 16, 14), Block.box(4, 14, 15, 12, 15, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape WEST_OPEN = Stream.of(Block.box(1, 1, 4, 2, 2, 5), Block.box(2, 0, 0, 16, 16, 16), Block.box(0, 1, 4, 1, 2, 12), Block.box(1, 1, 11, 2, 2, 12)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape WEST_CLOSED = Stream.of(Block.box(1, 14, 11, 2, 15, 12), Block.box(1, 14, 4, 2, 15, 5), Block.box(2, 0, 0, 16, 16, 16), Block.box(0, 14, 4, 1, 15, 12)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static final VoxelShape NORTH_COLLISION = Block.box(0, 0, 2, 16, 16, 16);
	private static final VoxelShape EAST_COLLISION = Block.box(0, 0, 0, 14, 16, 16);
	private static final VoxelShape SOUTH_COLLISION = Block.box(0, 0, 0, 16, 16, 14);
	private static final VoxelShape WEST_COLLISION = Block.box(2, 0, 0, 16, 16, 16);

	protected AbstractKeypadFurnaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(LIT, false).setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACING)) {
			case NORTH -> state.getValue(OPEN) ? NORTH_OPEN : NORTH_CLOSED;
			case EAST -> state.getValue(OPEN) ? EAST_OPEN : EAST_CLOSED;
			case SOUTH -> state.getValue(OPEN) ? SOUTH_OPEN : SOUTH_CLOSED;
			case WEST -> state.getValue(OPEN) ? WEST_OPEN : WEST_CLOSED;
			default -> Shapes.block();
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACING)) {
			case NORTH -> NORTH_COLLISION;
			case EAST -> EAST_COLLISION;
			case SOUTH -> SOUTH_COLLISION;
			case WEST -> WEST_COLLISION;
			default -> Shapes.block();
		};
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof Container container) {
				Containers.dropContents(level, pos, container);
				level.updateNeighbourForOutputSignal(pos, this);
			}

			//upstream drops modules through a global BlockEvent.BreakEvent handler that isn't ported yet, so (like
			//KeypadBlock) this drops them itself
			if (level.getBlockEntity(pos) instanceof AbstractKeypadFurnaceBlockEntity be)
				be.dropAllModules();
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof AbstractKeypadFurnaceBlockEntity be))
			return InteractionResult.PASS;

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (be.isDisabled())
				player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			else if (verifyPasscodeSet(pos, be, serverPlayer)) {
				if (be.isDenied(player)) {
					if (be.sendsDenylistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
				}
				else if (be.isAllowed(player)) {
					if (be.sendsAllowlistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

					activate(be, level, pos, player);
				}
				else {
					be.setPendingOpener(player.getUUID());
					NetworkHandler.openKeypadScreen(serverPlayer, pos, false, be.getOwner().getName());
				}
			}
		}

		return InteractionResult.SUCCESS;
	}

	/** Mirror of {@code KeypadBlock}'s passcode-set check: prompt the owner to set a code, warn everyone else. */
	private boolean verifyPasscodeSet(BlockPos pos, AbstractKeypadFurnaceBlockEntity be, ServerPlayer player) {
		if (be.hasPasscode())
			return true;

		Owner owner = be.getOwner();

		if (owner.isOwner(player))
			NetworkHandler.openKeypadScreen(player, pos, true, owner.getName());
		else
			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);

		return false;
	}

	/** Opens the furnace's screen for {@code player} directly, bypassing the passcode (allowlisted, or just-verified). */
	public void activate(AbstractKeypadFurnaceBlockEntity be, Level level, BlockPos pos, Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			level.gameEvent(player, GameEvent.CONTAINER_OPEN, pos);
			serverPlayer.openMenu(be);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof AbstractKeypadFurnaceBlockEntity be)
			be.recheckOpen();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		boolean waterlogged = ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER;

		return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(OPEN, false).setValue(LIT, false).setValue(WATERLOGGED, waterlogged);
	}

	@Override
	protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random) {
		if (state.getValue(WATERLOGGED))
			tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, level, tickAccess, pos, facing, facingPos, facingState, random);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN, LIT, WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/**
	 * Turns a vanilla furnace into its keypad counterpart when a Key Panel is used on it, and back again with the
	 * Universal Block Remover. 1:1 with upstream's inner class of the same name.
	 */
	public static class Convertible implements net.geforcemods.securitycraft.api.IPasscodeConvertible {
		private final Block unprotectedBlock;
		private final Block protectedBlock;

		public Convertible(Block unprotectedBlock, Block protectedBlock) {
			this.unprotectedBlock = unprotectedBlock;
			this.protectedBlock = protectedBlock;
		}

		@Override
		public boolean isUnprotectedBlock(BlockState state) {
			return state.is(unprotectedBlock);
		}

		@Override
		public boolean isProtectedBlock(BlockState state) {
			return state.is(protectedBlock);
		}

		@Override
		public boolean protect(Player player, Level level, BlockPos pos) {
			return convert(player, level, pos, protectedBlock, true);
		}

		@Override
		public boolean unprotect(Player player, Level level, BlockPos pos) {
			return convert(player, level, pos, unprotectedBlock, false);
		}

		private boolean convert(Player player, Level level, BlockPos pos, Block convertedBlock, boolean protect) {
			BlockState state = level.getBlockState(pos);
			Direction facing = state.getValue(FACING);
			boolean lit = state.getValue(LIT);
			net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace = (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity) level.getBlockEntity(pos);
			BlockState convertedState = convertedBlock.defaultBlockState().setValue(FACING, facing).setValue(LIT, lit);

			if (protect)
				convertedState = convertedState.setValue(OPEN, false);
			else
				((net.geforcemods.securitycraft.api.IModuleInventory) furnace).dropAllModules();

			net.minecraft.nbt.CompoundTag tag = furnace.saveWithFullMetadata(level.registryAccess());

			furnace.clearContent();
			level.setBlockAndUpdate(pos, convertedState);
			furnace = (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity) level.getBlockEntity(pos);
			furnace.loadWithComponents(tag, level.registryAccess());

			if (protect && player != null)
				((net.geforcemods.securitycraft.api.IOwnable) furnace).setOwner(player.getName().getString(), player.getUUID().toString());

			return true;
		}
	}
}
