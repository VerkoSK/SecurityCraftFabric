package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncAlarmSettingsPayload(BlockPos pos, ResourceLocation soundEvent, float pitch, int soundLength) {
	public static final ResourceLocation CHANNEL = SCContent.id("sync_alarm_settings");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeResourceLocation(soundEvent);
		buf.writeFloat(pitch);
		buf.writeVarInt(soundLength);
		return buf;
	}

	public static SyncAlarmSettingsPayload read(FriendlyByteBuf buf) {
		return new SyncAlarmSettingsPayload(buf.readBlockPos(), buf.readResourceLocation(), buf.readFloat(), buf.readVarInt());
	}
}
