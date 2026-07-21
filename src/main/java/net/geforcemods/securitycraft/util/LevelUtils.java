package net.geforcemods.securitycraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LevelUtils {
	private LevelUtils() {}

	public static <T extends BlockEntity> void blockEntityTicker(Level level, BlockPos pos, BlockState state, T be) {
		if (be instanceof ITickingBlockEntity ticking)
			ticking.tick(level, pos, state);
	}
}
