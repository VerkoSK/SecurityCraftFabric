package net.geforcemods.securitycraft.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Holds the owner of an ownable block (name + UUID). Faithful port of the concept from the
 * original SecurityCraft {@code api.Owner}, trimmed to what the Fabric core slice needs.
 */
public class Owner {
	public static final String DEFAULT_OWNER_NAME = "owner";
	public static final String DEFAULT_OWNER_UUID = "ownerUUID";

	private String name;
	private String uuid;

	public Owner() {
		this.name = DEFAULT_OWNER_NAME;
		this.uuid = DEFAULT_OWNER_UUID;
	}

	public Owner(String name, String uuid) {
		set(name, uuid);
	}

	public void set(String name, String uuid) {
		this.name = name == null ? DEFAULT_OWNER_NAME : name;
		this.uuid = uuid == null ? DEFAULT_OWNER_UUID : uuid;
	}

	public void set(Owner other) {
		set(other.getName(), other.getUUID());
	}

	public String getName() {
		return name;
	}

	public String getUUID() {
		return uuid;
	}

	/** Whether an owner has actually been assigned yet. */
	public boolean owns() {
		return !DEFAULT_OWNER_UUID.equals(uuid);
	}

	/** True if the given player is this owner. Matched by UUID. */
	public boolean isOwner(Player player) {
		if (!owns() || player == null)
			return false;

		return uuid.equals(player.getUUID().toString());
	}

	public void save(CompoundTag tag) {
		tag.putString(DEFAULT_OWNER_NAME, name);
		tag.putString(DEFAULT_OWNER_UUID, uuid);
	}

	public static Owner fromCompound(CompoundTag tag) {
		Owner owner = new Owner();

		// Since 1.21.5 CompoundTag#getString returns Optional<String>.
		if (tag != null)
			owner.set(tag.getString(DEFAULT_OWNER_NAME).orElse(DEFAULT_OWNER_NAME), tag.getString(DEFAULT_OWNER_UUID).orElse(DEFAULT_OWNER_UUID));

		return owner;
	}
}
