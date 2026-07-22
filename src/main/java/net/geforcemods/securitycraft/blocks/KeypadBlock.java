package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The keypad: an ownable, passcode-protected block. The placer becomes the owner and sets a code;
 * afterwards anyone may attempt the code, and a correct attempt emits a redstone pulse. Faithful 1:1
 * with upstream KeypadBlock (full 6-direction placement + rotation, waterlogging, module/allowlist
 * gating, configurable signal length), minus the NeoForge-only {@code shouldCheckWeakPower} hook and the
 * Frame-conversion / Codebreaker subsystems (separate blocks/items not present in this port).
 */
public class KeypadBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final DirectionProperty ROTATION = DirectionProperty.create("rotation", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public KeypadBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ROTATION, Direction.NORTH).setValue(POWERED, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeypadBlockEntity be))
			return InteractionResult.PASS;

		if (state.getValue(POWERED) && be.getSignalLength() > 0)
			return InteractionResult.PASS;
		else if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (be.isDisabled())
				player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			else if (verifyPasscodeSet(level, pos, be, serverPlayer)) {
				if (be.isDenied(player)) {
					if (be.sendsDenylistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
				}
				else if (be.isAllowed(player)) {
					if (be.sendsAllowlistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

					activate(state, level, pos, be.getSignalLength());
				}
				else
					NetworkHandler.openKeypadScreen(serverPlayer, pos, false, be.getOwner().getName());
			}
		}

		return InteractionResult.SUCCESS;
	}

	/** Mirror of upstream IPasscodeProtected.verifyPasscodeSet: prompt the owner to set a code, warn everyone else. */
	private boolean verifyPasscodeSet(Level level, BlockPos pos, KeypadBlockEntity be, ServerPlayer player) {
		if (be.hasPasscode())
			return true;

		Owner owner = be.getOwner();

		if (owner.isOwner(player))
			NetworkHandler.openKeypadScreen(player, pos, true, owner.getName());
		else
			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);

		return false;
	}

	public void activate(BlockState state, Level level, BlockPos pos, int signalLength) {
		level.setBlockAndUpdate(pos, state.cycle(POWERED));
		BlockUtils.updateIndirectNeighbors(level, pos, SCContent.KEYPAD);

		if (signalLength > 0)
			level.scheduleTick(pos, this, signalLength);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);

		if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof KeypadBlockEntity be)
			be.setOwner(player.getUUID().toString(), player.getName().getString());
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED)) {
			level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, pos, SCContent.KEYPAD);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		//prevents dropping twice the amount of modules when breaking the block in creative mode
		if (player.isCreative() && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.getInventory().clear();

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (state.getValue(POWERED))
				BlockUtils.updateIndirectNeighbors(level, pos, this);

			if (level.getBlockEntity(pos) instanceof IModuleInventory inv)
				inv.dropAllModules();
		}

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
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction facing = ctx.getNearestLookingDirection().getOpposite();
		boolean waterlogged = ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER;
		BlockState state = defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, waterlogged);

		if (facing == Direction.UP || facing == Direction.DOWN)
			return state.setValue(ROTATION, ctx.getHorizontalDirection().getOpposite());
		else
			return state.setValue(ROTATION, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(ROTATION, rot.rotate(state.getValue(ROTATION)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ROTATION, POWERED, WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadBlockEntity(pos, state);
	}

	/** Frame &#8596; Keypad conversion (right-clicking a Frame with a Key Panel item). Ported from upstream, minus the camera-feed guard. */
	public static class Convertible implements net.geforcemods.securitycraft.api.IPasscodeConvertible {
		@Override
		public boolean isUnprotectedBlock(BlockState state) {
			return state.is(SCContent.FRAME);
		}

		@Override
		public boolean isProtectedBlock(BlockState state) {
			return state.is(SCContent.KEYPAD);
		}

		@Override
		public boolean protect(Player player, Level level, BlockPos pos) {
			Owner owner = ((net.geforcemods.securitycraft.api.IOwnable) level.getBlockEntity(pos)).getOwner();
			Direction facing = level.getBlockState(pos).getValue(net.geforcemods.securitycraft.blocks.FrameBlock.FACING);

			level.setBlockAndUpdate(pos, SCContent.KEYPAD.defaultBlockState().setValue(KeypadBlock.FACING, facing).setValue(KeypadBlock.ROTATION, facing).setValue(KeypadBlock.POWERED, false));
			((net.geforcemods.securitycraft.api.IOwnable) level.getBlockEntity(pos)).setOwner(owner.getName(), owner.getUUID());
			return true;
		}

		@Override
		public boolean unprotect(Player player, Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			Owner owner = ((net.geforcemods.securitycraft.api.IOwnable) level.getBlockEntity(pos)).getOwner();
			Direction facing = state.getValue(KeypadBlock.FACING).getAxis().isHorizontal() ? state.getValue(KeypadBlock.FACING) : state.getValue(KeypadBlock.ROTATION);

			if (level.getBlockEntity(pos) instanceof IModuleInventory inv)
				inv.dropAllModules();

			level.setBlockAndUpdate(pos, SCContent.FRAME.defaultBlockState().setValue(net.geforcemods.securitycraft.blocks.FrameBlock.FACING, facing));
			((net.geforcemods.securitycraft.api.IOwnable) level.getBlockEntity(pos)).setOwner(owner.getName(), owner.getUUID());
			return true;
		}
	}
}
