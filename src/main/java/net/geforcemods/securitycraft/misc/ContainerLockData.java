package net.geforcemods.securitycraft.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-dimension registry of container blocks locked by position rather than by replacing the block. This is how the
 * Key Panel protects a container from a mod SecurityCraft doesn't otherwise recognise (i.e. it isn't a
 * {@link net.geforcemods.securitycraft.api.IPasscodeConvertible} match): instead of converting the block into a
 * keypad chest - which only works for blocks compatible with vanilla's {@code ChestBlock}/{@code ChestBlockEntity} -
 * this just remembers "this position belongs to this owner, with this passcode" and the global interaction/break
 * listeners in {@link ContainerLockEnforcement} enforce it, whatever the block actually is. Behaves the same as a
 * real passcode-protected chest (same set/check passcode screens); not part of upstream SecurityCraft.
 */
public class ContainerLockData extends SavedData {
	private final Map<Long, LockedContainer> locks = new HashMap<>();

	public static ContainerLockData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(ContainerLockData::load, ContainerLockData::new, "securitycraft_container_locks");
	}

	public static ContainerLockData load(CompoundTag tag) {
		ContainerLockData data = new ContainerLockData();
		ListTag list = tag.getList("locks", Tag.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++) {
			CompoundTag entryTag = list.getCompound(i);
			BlockPos pos = BlockPos.of(entryTag.getLong("pos"));
			LockedContainer entry = new LockedContainer(data, pos, new Owner(entryTag.getString("owner"), entryTag.getString("ownerUUID")));

			if (entryTag.contains("passcodeHash")) {
				entry.passcodeHash = entryTag.getString("passcodeHash");
				entry.salt = entryTag.getString("salt");
			}

			data.locks.put(pos.asLong(), entry);
		}

		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();

		for (LockedContainer entry : locks.values()) {
			CompoundTag entryTag = new CompoundTag();

			entryTag.putLong("pos", entry.pos.asLong());
			entryTag.putString("owner", entry.owner.getName());
			entryTag.putString("ownerUUID", entry.owner.getUUID());

			if (entry.passcodeHash != null) {
				entryTag.putString("passcodeHash", entry.passcodeHash);
				entryTag.putString("salt", entry.salt);
			}

			list.add(entryTag);
		}

		tag.put("locks", list);
		return tag;
	}

	public boolean isLocked(BlockPos pos) {
		return locks.containsKey(pos.asLong());
	}

	public LockedContainer get(BlockPos pos) {
		return locks.get(pos.asLong());
	}

	public void lock(BlockPos pos, Owner owner) {
		locks.put(pos.asLong(), new LockedContainer(this, pos.immutable(), owner.copy()));
		setDirty();
	}

	public void unlock(BlockPos pos) {
		if (locks.remove(pos.asLong()) != null)
			setDirty();
	}

	/** One locked position. Behaves like {@link PasscodeProtected} so it can reuse the set/check passcode screens. */
	public static class LockedContainer implements PasscodeProtected {
		private final ContainerLockData parent;
		private final BlockPos pos;
		private final Owner owner;
		private String passcodeHash;
		private String salt;
		private UUID pendingOpener;

		private LockedContainer(ContainerLockData parent, BlockPos pos, Owner owner) {
			this.parent = parent;
			this.pos = pos;
			this.owner = owner;
		}

		@Override
		public boolean hasPasscode() {
			return passcodeHash != null;
		}

		@Override
		public void setPasscode(String passcode) {
			salt = UUID.randomUUID().toString();
			passcodeHash = PasscodeUtils.hash(passcode, salt);
			parent.setDirty();
		}

		@Override
		public boolean checkPasscode(String attempt) {
			return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
		}

		@Override
		public Owner getOwner() {
			return owner;
		}

		/** Remembers who is currently attempting the passcode, so a correct attempt opens the container for them. */
		public void setPendingOpener(Player player) {
			pendingOpener = player.getUUID();
		}

		@Override
		public void activate(ServerLevel level) {
			if (pendingOpener == null)
				return;

			Player player = level.getPlayerByUUID(pendingOpener);

			pendingOpener = null;

			//let the block's own use() open it, instead of guessing at a menu ourselves: some mods build their
			//container menu (size, tier, extra data) inside use() itself rather than through a plain MenuProvider,
			//so opening it any other way risks a wrong-sized or broken screen
			if (player instanceof ServerPlayer) {
				net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

				state.use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false));
			}
		}

		@Override
		public void startCooldown() {}

		@Override
		public long getCooldownEnd() {
			return 0;
		}

		@Override
		public boolean isOnCooldown() {
			return false;
		}
	}
}
