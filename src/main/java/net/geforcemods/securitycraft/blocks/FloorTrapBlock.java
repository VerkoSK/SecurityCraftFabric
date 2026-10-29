package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.FloorTrapBlockEntity;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FloorTrapBlock extends SometimesVisibleBlock {
	public FloorTrapBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
		if (pos.getY() == neighborPos.getY() && level.getBlockEntity(pos) instanceof FloorTrapBlockEntity trap1 && !trap1.isDisabled() && neighborStateIsActivatedTrap(level, neighborPos) && level.getBlockEntity(neighborPos) instanceof FloorTrapBlockEntity trap2 && trap1.isOwnedBy(trap2.getOwner()) && trap1.shouldDisappearInstantlyInChains())
			trap1.scheduleDisappear(0, true);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
		return (adjacentState.is(this) && !adjacentState.getValue(INVISIBLE)) || super.skipRendering(state, adjacentState, side);
	}

	public static boolean neighborStateIsActivatedTrap(LevelAccessor level, BlockPos neighborPos) {
		BlockState neighborState = level.getBlockState(neighborPos);

		return neighborState.is(SCContent.FLOOR_TRAP) && neighborState.getValue(INVISIBLE);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return LevelUtils.createTickerHelper(type, SCContent.FLOOR_TRAP_BLOCK_ENTITY, LevelUtils::blockEntityTicker);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FloorTrapBlockEntity(pos, state);
	}
}
