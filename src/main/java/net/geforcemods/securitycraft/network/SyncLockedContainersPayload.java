package net.geforcemods.securitycraft.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: sync the list of positions locked by a Key Panel (ContainerLockData). */
public record SyncLockedContainersPayload(boolean clearExisting, List<BlockPos> locked, List<BlockPos> unlocked) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "sync_locked_containers");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBoolean(clearExisting);
		buf.writeVarInt(locked.size());
		for (BlockPos pos : locked) {
			buf.writeBlockPos(pos);
		}
		buf.writeVarInt(unlocked.size());
		for (BlockPos pos : unlocked) {
			buf.writeBlockPos(pos);
		}
		return buf;
	}

	public static SyncLockedContainersPayload read(FriendlyByteBuf buf) {
		boolean clearExisting = buf.readBoolean();

		int lockedCount = buf.readVarInt();
		List<BlockPos> locked = new ArrayList<>(lockedCount);
		for (int i = 0; i < lockedCount; i++) {
			locked.add(buf.readBlockPos());
		}

		int unlockedCount = buf.readVarInt();
		List<BlockPos> unlocked = new ArrayList<>(unlockedCount);
		for (int i = 0; i < unlockedCount; i++) {
			unlocked.add(buf.readBlockPos());
		}

		return new SyncLockedContainersPayload(clearExisting, locked, unlocked);
	}
}
