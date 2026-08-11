package net.geforcemods.securitycraft.misc;

import net.minecraft.world.entity.player.Player;

/** A screen that closes itself once the player no longer meets its condition. 1:1 with the upstream interface of the same name. */
public interface StillValid {
	boolean stillValid(Player player);
}
