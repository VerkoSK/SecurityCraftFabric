package net.geforcemods.securitycraft.util;

import java.util.function.BiPredicate;

import net.geforcemods.securitycraft.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Small block helpers ported from the original SecurityCraft {@code util.BlockUtils}. */
public class BlockUtils {
	private BlockUtils() {}

	/**
	 * MC 1.21.6 routes block-entity deserialization through {@link net.minecraft.world.level.storage.ValueInput}
	 * instead of a raw {@link CompoundTag}. These bridge a {@link CompoundTag} (still produced by
	 * {@code saveWithFullMetadata}/{@code saveCustomOnly}) back into the new load methods.
	 */
	public static void loadBlockEntityWithComponents(net.minecraft.world.level.block.entity.BlockEntity be, CompoundTag tag, HolderLookup.Provider registries) {
		be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
	}

	public static void loadBlockEntityCustomOnly(net.minecraft.world.level.block.entity.BlockEntity be, CompoundTag tag, HolderLookup.Provider registries) {
		be.loadCustomOnly(net.minecraft.world.level.storage.TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
	}

	/** Removes blocks in a straight line from {@code pos} along each direction, as long as {@code stateMatcher} keeps matching. */
	public static void removeInSequence(BiPredicate<Direction, BlockState> stateMatcher, LevelAccessor level, BlockPos pos, Direction... directions) {
		for (Direction direction : directions) {
			int i = 1;
			BlockPos modifiedPos = pos.relative(direction, i);

			while (stateMatcher.test(direction, level.getBlockState(modifiedPos))) {
				level.removeBlock(modifiedPos, false);
				modifiedPos = pos.relative(direction, ++i);
			}
		}
	}

	public static boolean isSideSolid(LevelReader level, BlockPos pos, Direction side) {
		return level.getBlockState(pos).isFaceSturdy(level, pos, side);
	}

	public static ExplosionInteraction getExplosionInteraction() {
		return ConfigHandler.mineExplosionsBreakBlocks ? ExplosionInteraction.BLOCK : ExplosionInteraction.NONE;
	}

	public static void updateIndirectNeighbors(Level level, BlockPos pos, Block block) {
		updateIndirectNeighbors(level, pos, block, Direction.values());
	}

	public static void updateIndirectNeighbors(Level level, BlockPos pos, Block block, Direction... directions) {
		level.updateNeighborsAt(pos, block);

		for (Direction dir : directions) {
			level.updateNeighborsAt(pos.relative(dir), block);
		}
	}
}
