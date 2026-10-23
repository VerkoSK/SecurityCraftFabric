package net.geforcemods.securitycraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This interface marks a BlockEntity as being lockable by SecurityCraft's Sonic Security System.
 */
public interface ILockable {
	default BlockEntity getThisBlockEntity() {
		return (BlockEntity) this;
	}

	default boolean isLockedBySSS() {
		return false;
	}

	default boolean disableInteractionWhenLocked(Level level, BlockPos pos, Player player) {
		return true;
	}
}
