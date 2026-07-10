package net.geforcemods.securitycraft.api;

import net.minecraft.world.entity.player.Player;

/** Anything (block entity) that can be owned by a player. */
public interface IOwnable {
	Owner getOwner();

	void setOwner(String name, String uuid);

	default boolean isOwnedBy(Player player) {
		return getOwner().isOwner(player);
	}
}
