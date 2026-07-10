package net.geforcemods.securitycraft.api;

import net.minecraft.entity.player.PlayerEntity;

/** Anything (block entity) that can be owned by a player. */
public interface IOwnable {
	Owner getOwner();

	void setOwner(String name, String uuid);

	default boolean isOwnedBy(PlayerEntity player) {
		return getOwner().isOwner(player);
	}
}
