package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * The keypad: an ownable, passcode-protected block. First player to use it becomes the owner and
 * sets a code; afterwards anyone may attempt the code, and a correct attempt emits a redstone pulse.
 */
public class KeypadBlock extends Block implements BlockEntityProvider {
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;

	public KeypadBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(POWERED))
			return ActionResult.PASS;

		if (world.isClient())
			return ActionResult.SUCCESS;

		if (!(world.getBlockEntity(pos) instanceof KeypadBlockEntity be) || !(player instanceof ServerPlayerEntity serverPlayer))
			return ActionResult.PASS;

		Owner owner = be.getOwner();

		if (!owner.owns()) {
			be.setOwner(player.getName().getString(), player.getUuid().toString());
			NetworkHandler.openKeypadScreen(serverPlayer, pos, true, player.getName().getString());
		}
		else if (owner.isOwner(player) && player.isSneaking()) {
			NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
		}
		else if (!be.hasPasscode()) {
			if (owner.isOwner(player))
				NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
			else
				player.sendMessage(Text.translatable("messages.securitycraft:passcode.notSetUp"), true);
		}
		else
			NetworkHandler.openKeypadScreen(serverPlayer, pos, false, owner.getName());

		return ActionResult.CONSUME;
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, false), 3);
			world.updateNeighborsAlways(pos, this, null);
		}
	}

	@Override
	protected boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(POWERED) ? 15 : 0;
	}

	@Override
	protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return getWeakRedstonePower(state, world, pos, direction);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadBlockEntity(pos, state);
	}
}
