package net.geforcemods.securitycraft.misc;

import java.util.HashMap;
import java.util.Map;

import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-dimension registry of container blocks locked by position rather than by replacing the block. This is how the
 * Key Panel protects a container from a mod SecurityCraft doesn't otherwise recognise (i.e. it isn't a
 * {@link net.geforcemods.securitycraft.api.IPasscodeConvertible} match): instead of converting the block into a
 * keypad chest - which only works for blocks compatible with vanilla's {@code ChestBlock}/{@code ChestBlockEntity} -
 * this just remembers "this position belongs to this owner" and the global interaction/break listeners enforce it,
 * whatever the block actually is. Not part of upstream SecurityCraft.
 */
public class ContainerLockData extends SavedData {
	private final Map<Long, Owner> locks = new HashMap<>();

	public static ContainerLockData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(ContainerLockData::load, ContainerLockData::new, "securitycraft_container_locks");
	}

	public static ContainerLockData load(CompoundTag tag) {
		ContainerLockData data = new ContainerLockData();
		ListTag list = tag.getList("locks", Tag.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);

			data.locks.put(entry.getLong("pos"), new Owner(entry.getString("owner"), entry.getString("ownerUUID")));
		}

		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();

		for (Map.Entry<Long, Owner> entry : locks.entrySet()) {
			CompoundTag entryTag = new CompoundTag();

			entryTag.putLong("pos", entry.getKey());
			entryTag.putString("owner", entry.getValue().getName());
			entryTag.putString("ownerUUID", entry.getValue().getUUID());
			list.add(entryTag);
		}

		tag.put("locks", list);
		return tag;
	}

	public boolean isLocked(BlockPos pos) {
		return locks.containsKey(pos.asLong());
	}

	public Owner getOwner(BlockPos pos) {
		return locks.get(pos.asLong());
	}

	public void lock(BlockPos pos, Owner owner) {
		locks.put(pos.asLong(), owner.copy());
		setDirty();
	}

	public void unlock(BlockPos pos) {
		if (locks.remove(pos.asLong()) != null)
			setDirty();
	}
}
