package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thin wall/floor/ceiling panel form of the {@link KeycardReaderBlockEntity} - same programming screen and link
 * logic, just mounted like a button instead of standing free.
 */
public class KeycardLockBlockEntity extends KeycardReaderBlockEntity {
	public KeycardLockBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYCARD_LOCK_BLOCK_ENTITY, pos, state);
	}

	/** Whether the owner has changed anything from the defaults yet (signature or an accepted level). */
	public boolean isSetUp() {
		if (getSignature() != 0)
			return true;

		boolean[] levels = getAcceptedLevels();

		return !levels[0] || levels[1] || levels[2] || levels[3] || levels[4];
	}
}
