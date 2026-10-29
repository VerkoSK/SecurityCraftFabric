package net.geforcemods.securitycraft.util;

import java.util.function.BiPredicate;

import net.geforcemods.securitycraft.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Small block helpers ported from the original SecurityCraft {@code util.BlockUtils}. */
public class BlockUtils {
	private BlockUtils() {}

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

	public static boolean areItemsEqual(net.minecraft.world.item.ItemStack firstItemStack, net.minecraft.world.item.ItemStack secondItemStack, boolean exactComponentCheck) {
		return exactComponentCheck ? net.minecraft.world.item.ItemStack.isSameItemSameTags(firstItemStack, secondItemStack) : firstItemStack.is(secondItemStack.getItem());
	}

	public static boolean isInsideUnownedReinforcedBlocks(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.Vec3 pos, float entityWidth) {
		float width = entityWidth * 0.8F;
		net.minecraft.world.phys.AABB inWallArea = net.minecraft.world.phys.AABB.ofSize(pos, width, 1.0E-6, width);

		return BlockPos.betweenClosedStream(inWallArea).anyMatch(testPos -> {
			BlockState wallState = level.getBlockState(testPos);

			return wallState.getBlock() instanceof net.geforcemods.securitycraft.api.IReinforcedBlock && wallState.isSuffocating(level, testPos) && (!(level.getBlockEntity(testPos) instanceof net.geforcemods.securitycraft.api.IOwnable ownable) || !ownable.isOwnedBy(player)) && net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(wallState.getCollisionShape(level, testPos).move(testPos.getX(), testPos.getY(), testPos.getZ()), net.minecraft.world.phys.shapes.Shapes.create(inWallArea), net.minecraft.world.phys.shapes.BooleanOp.AND);
		});
	}
}
