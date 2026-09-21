package net.geforcemods.securitycraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LevelUtils {
	private LevelUtils() {}

	public static <T extends BlockEntity> void blockEntityTicker(Level level, BlockPos pos, BlockState state, T be) {
		if (be instanceof ITickingBlockEntity ticking)
			ticking.tick(level, pos, state);
	}

	/** Public stand-in for {@code BaseEntityBlock.createTickerHelper}, which is protected and thus unreachable from blocks that do not extend it. */
	@SuppressWarnings("unchecked")
	public static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}
}
