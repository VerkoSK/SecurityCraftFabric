package net.geforcemods.securitycraft.blocks;

import com.mojang.serialization.MapCodec;

import net.geforcemods.securitycraft.blockentities.FrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Frame: an owned iron frame block. Upstream it displays a live camera feed; this port keeps the
 * block (shape, placement, owner) so it can be converted into a keypad with a Key Panel. The camera
 * subsystem is not ported, so the frame is otherwise decorative.
 */
public class FrameBlock extends OwnableBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<FrameBlock> CODEC = simpleCodec(FrameBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE_NORTH = Shapes.joinUnoptimized(Shapes.block(), Block.box(1, 1, 0, 15, 15, 1), BooleanOp.ONLY_FIRST);
	private static final VoxelShape SHAPE_EAST = Shapes.joinUnoptimized(Shapes.block(), Block.box(15, 1, 1, 16, 15, 15), BooleanOp.ONLY_FIRST);
	private static final VoxelShape SHAPE_SOUTH = Shapes.joinUnoptimized(Shapes.block(), Block.box(1, 1, 15, 15, 15, 16), BooleanOp.ONLY_FIRST);
	private static final VoxelShape SHAPE_WEST = Shapes.joinUnoptimized(Shapes.block(), Block.box(0, 1, 1, 1, 15, 15), BooleanOp.ONLY_FIRST);

	public FrameBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACING)) {
			case NORTH -> SHAPE_NORTH;
			case EAST -> SHAPE_EAST;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			default -> Shapes.empty();
		};
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED))
			tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, WATERLOGGED);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathType) {
		return false;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FrameBlockEntity(pos, state);
	}
}
