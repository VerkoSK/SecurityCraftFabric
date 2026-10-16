package net.geforcemods.securitycraft.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityTracker<BE extends BlockEntity> {
	private final Map<ResourceKey<Level>, Collection<BlockPos>> trackedBlockEntities = new ConcurrentHashMap<>();
	private final FunctionRange<BE> range;

	public BlockEntityTracker(FunctionRange<BE> range) {
		this.range = range;
	}

	public void track(BE be) {
		getTrackedBlockEntities(be.getLevel()).add(be.getBlockPos().immutable());
	}

	public void stopTracking(BE be) {
		getTrackedBlockEntities(be.getLevel()).remove(be.getBlockPos());
	}

	public void clear() {
		trackedBlockEntities.clear();
	}

	public List<BE> getBlockEntitiesInRange(Level level, BlockPos pos) {
		return getBlockEntitiesInRange(level, new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
	}

	public List<BE> getBlockEntitiesInRange(Level level, Vec3 pos) {
		return getBlockEntitiesWithCondition(level, be -> canReach(be, pos));
	}

	public List<BE> getBlockEntitiesAround(Level level, BlockPos pos, int radius) {
		return iterate(level, ( list, bePos) -> {
			if (Math.abs(pos.getX() - bePos.getX()) <= radius && Math.abs(pos.getY() - bePos.getY()) <= radius && Math.abs(pos.getZ() - bePos.getZ()) <= radius)
				return (BE) level.getBlockEntity(bePos);

			return null;
		});
	}

	public List<BE> getBlockEntitiesWithCondition(Level level, Predicate<BE> condition) {
		return iterate(level, (list, bePos) -> {
			BE be = (BE) level.getBlockEntity(bePos);

			if (be != null && condition.test(be))
				return be;

			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private List<BE> iterate(Level level, BiFunction<List<BE>, BlockPos, BE> matcher) {
		final Collection<BlockPos> blockEntities = getTrackedBlockEntities(level);
		List<BE> returnValue = new ArrayList<>();

		for (Iterator<BlockPos> it = blockEntities.iterator(); it.hasNext();) {
			BlockPos bePos = it.next();

			if (bePos != null) {
				try {
					if (level.isLoaded(bePos)) {
						BE be = matcher.apply(returnValue, bePos);

						if (be != null)
							returnValue.add(be);

						continue;
					}
				}
				catch (Exception e) {}
			}

			it.remove();
		}

		return returnValue;
	}

	public Collection<BlockPos> getTrackedBlockEntities(Level level) {
		Collection<BlockPos> blockEntities = trackedBlockEntities.get(level.dimension());

		if (blockEntities == null) {
			blockEntities = ConcurrentHashMap.newKeySet();
			trackedBlockEntities.put(level.dimension(), blockEntities);
		}

		return blockEntities;
	}

	public boolean canReach(BE be, Vec3 pos) {
		AABB rangeBox = new AABB(be.getBlockPos()).inflate(range.apply(be));

		return rangeBox.minX <= pos.x() && rangeBox.minY <= pos.y() && rangeBox.minZ <= pos.z() && rangeBox.maxX >= pos.x() && rangeBox.maxY >= pos.y() && rangeBox.maxZ >= pos.z();
	}

	@FunctionalInterface
	public interface FunctionRange<BE> {
		int apply(BE be);
	}
}
