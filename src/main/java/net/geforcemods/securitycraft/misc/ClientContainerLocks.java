package net.geforcemods.securitycraft.misc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;

/** Client-side mirror of positions currently locked by a Key Panel via ContainerLockData. */
public final class ClientContainerLocks {
	private static final Set<Long> LOCKED_POSITIONS = new HashSet<>();

	private ClientContainerLocks() {}

	public static void handle(boolean clearExisting, Collection<BlockPos> locked, Collection<BlockPos> unlocked) {
		if (clearExisting)
			LOCKED_POSITIONS.clear();

		for (BlockPos pos : locked) {
			LOCKED_POSITIONS.add(pos.asLong());
		}

		for (BlockPos pos : unlocked) {
			LOCKED_POSITIONS.remove(pos.asLong());
		}
	}

	public static boolean isLocked(BlockPos pos) {
		return LOCKED_POSITIONS.contains(pos.asLong());
	}

	public static void clear() {
		LOCKED_POSITIONS.clear();
	}
}
