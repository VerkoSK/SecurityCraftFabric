package net.geforcemods.securitycraft.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

/** Holds the owner of an ownable block (name + UUID). */
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

	public boolean owns() {
		return !DEFAULT_OWNER_UUID.equals(uuid);
	}

	public boolean isOwner(PlayerEntity player) {
		if (!owns() || player == null)
			return false;

		return uuid.equals(player.getUuid().toString());
	}

	public void save(WriteView view) {
		view.putString(DEFAULT_OWNER_NAME, name);
		view.putString(DEFAULT_OWNER_UUID, uuid);
	}

	// Since 1.21.6 block entities serialize through ReadView/WriteView.
	public static Owner load(ReadView view) {
		Owner owner = new Owner();

		owner.set(view.getString(DEFAULT_OWNER_NAME, DEFAULT_OWNER_NAME), view.getString(DEFAULT_OWNER_UUID, DEFAULT_OWNER_UUID));
		return owner;
	}
}
