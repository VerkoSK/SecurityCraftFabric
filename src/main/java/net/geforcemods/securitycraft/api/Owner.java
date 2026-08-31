package net.geforcemods.securitycraft.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Holds the owner of an ownable block (name + UUID). Faithful port of the concept from the
 * original SecurityCraft {@code api.Owner}, trimmed to what the Fabric core slice needs.
 */
public class Owner {
	public static final String DEFAULT_OWNER_NAME = "owner";
	public static final String DEFAULT_OWNER_UUID = "ownerUUID";

	private String name;
	private String uuid;
	private boolean validated = true;

	public Owner() {
		this.name = DEFAULT_OWNER_NAME;
		this.uuid = DEFAULT_OWNER_UUID;
	}

	public Owner(String name, String uuid) {
		set(name, uuid);
	}

	public Owner(String name, String uuid, boolean validated) {
		set(name, uuid);
		this.validated = validated;
	}

	public Owner(Player player) {
		if (player != null)
			set(player.getName().getString(), player.getGameProfile().getId().toString());
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

	/** @return whether this owner owns every given ownable. */
	public boolean owns(IOwnable... ownables) {
		for (IOwnable ownable : ownables) {
			if (ownable != null && !ownable.isOwnedBy(this))
				return false;
		}

		return true;
	}

	/**
	 * Whether the other owner should be treated as the same owner as this one. Matched by UUID, falling
	 * back to name when either side has no UUID. (The upstream also considers teams; teams aren't ported.)
	 */
	public boolean isTreatedTheSameAs(Owner otherOwner) {
		String selfUUID = getUUID();
		String otherUUID = otherOwner.getUUID();
		String otherName = otherOwner.getName();

		if (otherUUID != null && otherUUID.equals(selfUUID))
			return true;

		if (otherName != null && (selfUUID.equals(DEFAULT_OWNER_UUID) || otherUUID.equals(DEFAULT_OWNER_UUID)) && otherName.equals(getName()))
			return true;

		//two different players still count as the same owner while they share a scoreboard team, if the server
		//turned that on; TeamUtils returns false outright when the option is off, so this costs nothing otherwise
		return net.geforcemods.securitycraft.util.TeamUtils.areOnSameTeam(this, otherOwner);
	}

	public Owner copy() {
		return new Owner(name, uuid, validated);
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	/** @return true if this owner has no player data attached (the fallback owner object). */
	public boolean isDefaultOwner() {
		return equals(new Owner());
	}

	public void save(ValueOutput output) {
		save(output, false);
	}

	public void save(ValueOutput output, boolean saveValidationStatus) {
		output.putString(DEFAULT_OWNER_NAME, name);
		output.putString(DEFAULT_OWNER_UUID, uuid);

		if (saveValidationStatus)
			output.putBoolean("ownerValidated", validated);
	}

	// Since 1.21.6 block entities serialize through ValueInput/ValueOutput.
	// Reads owner, ownerUUID and ownerValidated independently (each optional; absent keys keep defaults).
	public static Owner load(ValueInput input) {
		Owner owner = new Owner();

		owner.name = input.getStringOr(DEFAULT_OWNER_NAME, DEFAULT_OWNER_NAME);
		owner.uuid = input.getStringOr(DEFAULT_OWNER_UUID, DEFAULT_OWNER_UUID);
		owner.validated = input.getBooleanOr("ownerValidated", true);
		return owner;
	}

	/** Reads the owner straight out of a raw block-entity {@link CompoundTag}, for the few call sites (moving pistons) that only have the tag. */
	public static Owner fromTag(CompoundTag tag) {
		Owner owner = new Owner();

		owner.name = tag.getStringOr(DEFAULT_OWNER_NAME, DEFAULT_OWNER_NAME);
		owner.uuid = tag.getStringOr(DEFAULT_OWNER_UUID, DEFAULT_OWNER_UUID);
		owner.validated = tag.getBooleanOr("ownerValidated", true);
		return owner;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Owner owner && getName().equals(owner.getName()) && getUUID().equals(owner.getUUID());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(name, uuid);
	}

	@Override
	public String toString() {
		return "Name: " + name + "  UUID: " + uuid;
	}
}
