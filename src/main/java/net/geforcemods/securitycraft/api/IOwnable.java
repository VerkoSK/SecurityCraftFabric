package net.geforcemods.securitycraft.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

/** Anything (block entity) that can be owned by a player. */
public interface IOwnable {
	Owner getOwner();

	void setOwner(String name, String uuid);

	default boolean isOwnedBy(Player player) {
		return player != null && getOwner().owns() && getOwner().isTreatedTheSameAs(new Owner(player));
	}

	/** @return true if the owner should be invalidated when changed by the Universal Owner Changer (not ported). */
	default boolean needsValidation() {
		return false;
	}

	/** Called when this is validated. */
	default void onValidate() {}

	/** Incognito-mask isn't ported, so {@code ignoreMask} has no effect; kept for upstream-signature parity. */
	default boolean isOwnedBy(Player player, boolean ignoreMask) {
		return isOwnedBy(player);
	}

	default boolean isOwnedBy(Entity entity) {
		return entity instanceof Player player && isOwnedBy(player);
	}

	default boolean isOwnedBy(Owner otherOwner) {
		return getOwner().isTreatedTheSameAs(otherOwner);
	}

	/** Whether this ownable ignores its owner (owner is not exempt from its effects). */
	default boolean ignoresOwner() {
		return false;
	}

	/** Whether an ownable entity (e.g. a tamed pet) belongs to the same owner as this. */
	default boolean allowsOwnableEntity(OwnableEntity entity) {
		return entity.getOwnerUUID() != null && entity.getOwnerUUID().toString().equals(getOwner().getUUID());
	}
}
