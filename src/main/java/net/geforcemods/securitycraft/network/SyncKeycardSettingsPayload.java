package net.geforcemods.securitycraft.network;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: the keycard reader/lock screen's accepted levels and signature (kept live so the block reflects
 * changes immediately), and, if {@code link} is set, links the keycard currently in the slot with those settings.
 */
public record SyncKeycardSettingsPayload(BlockPos pos, boolean[] acceptedLevels, int signature, boolean link, Optional<String> usableBy) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "sync_keycard_settings");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);

		for (boolean accepted : acceptedLevels) {
			buf.writeBoolean(accepted);
		}

		buf.writeVarInt(signature);
		buf.writeBoolean(link);
		buf.writeOptional(usableBy, FriendlyByteBuf::writeUtf);
		return buf;
	}

	public static SyncKeycardSettingsPayload read(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		boolean[] acceptedLevels = new boolean[5];

		for (int i = 0; i < 5; i++) {
			acceptedLevels[i] = buf.readBoolean();
		}

		int signature = buf.readVarInt();
		boolean link = buf.readBoolean();
		Optional<String> usableBy = buf.readOptional(FriendlyByteBuf::readUtf);

		return new SyncKeycardSettingsPayload(pos, acceptedLevels, signature, link, usableBy);
	}
}
