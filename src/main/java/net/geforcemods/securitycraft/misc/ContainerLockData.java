package net.geforcemods.securitycraft.misc;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

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

	private static final Codec<ContainerLockData> CODEC = CompoundTag.CODEC.xmap(ContainerLockData::load, data -> data.save(new CompoundTag()));
	private static final SavedDataType<ContainerLockData> TYPE = new SavedDataType<>("securitycraft_container_locks", ContainerLockData::new, CODEC, null);

	public static ContainerLockData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public static ContainerLockData load(CompoundTag tag) {
		ContainerLockData data = new ContainerLockData();
		ListTag list = tag.getListOrEmpty("locks");

		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompoundOrEmpty(i);

			data.locks.put(entry.getLongOr("pos", 0L), new Owner(entry.getStringOr("owner", "owner"), entry.getStringOr("ownerUUID", "ownerUUID")));
		}

		return data;
	}

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
		locks.put(pos.asLong(), owner);
		setDirty();
	}

	public void unlock(BlockPos pos) {
		locks.remove(pos.asLong());
		setDirty();
	}
}
