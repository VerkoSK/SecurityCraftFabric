package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DisguisableBlock extends OwnableBlock implements SimpleWaterloggedBlock, IDisguisable {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	protected DisguisableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static boolean isNormalCube(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != state.getBlock())
			return disguisedState.isRedstoneConductor(level, pos);
		else
			return state.isSolid() && state.isCollisionShapeFullBlock(level, pos);
	}

	public static boolean isSuffocating(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != state.getBlock())
			return disguisedState.isSuffocating(level, pos);
		else
			return state.blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, disguisedState, player, level, pos);
		else
			return super.getDestroyProgress(state, player, level, pos);
	}

	public final int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getLightEmission();
		else
			return state.getLightEmission();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
	}

	public SoundType getSoundType(BlockState state, LevelAccessor level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getSoundType();
		else
			return super.getSoundType(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getShape(level, pos, ctx);
		else
			return super.getShape(state, level, pos, ctx);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getCollisionShape(level, pos, ctx);
		else
			return super.getCollisionShape(state, level, pos, ctx);
	}

	@Override
	public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random) {
		if (state.getValue(WATERLOGGED))
			tickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, level, tickAccess, currentPos, facing, facingPos, facingState, random);
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getShadeBrightness(level, pos);
		else
			return super.getShadeBrightness(state, level, pos);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getVisualShape(level, pos, ctx);
		else
			return super.getVisualShape(state, level, pos, ctx);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
		Block block = this;

		if (adjacentState.getBlock() instanceof IDisguisable)
			return false;

		return (adjacentState.is(block) ? true : super.skipRendering(state, adjacentState, side));
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState disguisedState = IDisguisable.getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getBlockSupportShape(level, pos);
		else
			return Shapes.empty();
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(this);
	}
}
