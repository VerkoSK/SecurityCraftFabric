package net.geforcemods.securitycraft.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Per-dimension registry of container blocks locked by position rather than by replacing the block. This is how the
 * Key Panel protects a container from a mod SecurityCraft doesn't otherwise recognise (i.e. it isn't a
 * {@link net.geforcemods.securitycraft.api.IPasscodeConvertible} match): instead of converting the block into a
 * keypad chest - which only works for blocks compatible with vanilla's {@code ChestBlock}/{@code ChestBlockEntity} -
 * this just remembers "this position belongs to this owner, with this passcode" and the global interaction/break
 * listeners in {@link ContainerLockEnforcement} enforce it, whatever the block actually is. Behaves the same as a
 * real passcode-protected chest (same set/check passcode screens); not part of upstream SecurityCraft.
 * <p>
 * A container made of two adjacent blocks of the same type (a modded "double chest" that doesn't merge through
 * vanilla's {@code ChestType}/{@code DoubleBlockCombiner}, so the tag-based conversion above can't catch it either)
 * locks both halves together: {@link #lock} looks for a same-block horizontal neighbour and covers it too, so the
 * container can be opened - and requires the same passcode - from either side.
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
			Owner owner = new Owner(entryTag.getString("owner"), entryTag.getString("ownerUUID"));
			List<BlockPos> positions = new ArrayList<>();

			for (Tag posTag : entryTag.getList("positions", Tag.TAG_LONG)) {
				positions.add(BlockPos.of(((LongTag) posTag).getAsLong()));
			}

			if (positions.isEmpty())
				continue;

			LockedContainer entry = new LockedContainer(data, positions, owner);

			if (entryTag.contains("passcodeHash")) {
				entry.passcodeHash = entryTag.getString("passcodeHash");
				entry.salt = entryTag.getString("salt");
			}

			for (BlockPos pos : positions) {
				data.locks.put(pos.asLong(), entry);
			}
		}

		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();

		//a two-position lock is stored under two map keys pointing at the same object; save it once
		for (LockedContainer entry : new HashSet<>(locks.values())) {
			CompoundTag entryTag = new CompoundTag();
			ListTag positions = new ListTag();

			for (BlockPos pos : entry.positions) {
				positions.add(LongTag.valueOf(pos.asLong()));
			}

			entryTag.put("positions", positions);
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

	/** Locks {@code pos}, and - if it looks like one half of a double container - its other half too. */
	public void lock(Level level, BlockPos pos, Owner owner) {
		List<BlockPos> positions = new ArrayList<>();

		positions.add(pos.immutable());

		BlockPos partner = findPartner(level, pos);

		if (partner != null && !isLocked(partner))
			positions.add(partner);

		LockedContainer entry = new LockedContainer(this, positions, owner.copy());

		for (BlockPos p : positions) {
			locks.put(p.asLong(), entry);
		}

		setDirty();
	}

	/** Unlocks {@code pos} and, if it was locked together with another half, that position too. */
	public void unlock(BlockPos pos) {
		LockedContainer entry = locks.remove(pos.asLong());

		if (entry == null)
			return;

		for (BlockPos p : entry.positions) {
			locks.remove(p.asLong());
		}

		setDirty();
	}

	/** A same-block horizontal neighbour that also holds an inventory, i.e. likely the other half of a double container. */
	private static BlockPos findPartner(Level level, BlockPos pos) {
		Block block = level.getBlockState(pos).getBlock();

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos neighbor = pos.relative(dir);

			if (level.getBlockState(neighbor).getBlock() == block && level.getBlockEntity(neighbor) instanceof Container)
				return neighbor;
		}

		return null;
	}

	/** One locked container, one or two positions. Behaves like {@link PasscodeProtected} so it can reuse the set/check passcode screens. */
	public static class LockedContainer implements PasscodeProtected {
		private final ContainerLockData parent;
		private final List<BlockPos> positions;
		private final Owner owner;
		private String passcodeHash;
		private String salt;
		private UUID pendingOpener;

		private LockedContainer(ContainerLockData parent, List<BlockPos> positions, Owner owner) {
			this.parent = parent;
			this.positions = positions;
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
			//so opening it any other way risks a wrong-sized or broken screen. Either half works for a double
			//container - the block's own merge logic finds its partner, same as right-clicking it normally would.
			if (player instanceof ServerPlayer) {
				BlockPos pos = positions.get(0);
				BlockState state = level.getBlockState(pos);

				state.use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
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
