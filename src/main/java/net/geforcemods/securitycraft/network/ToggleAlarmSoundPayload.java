package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;

public record ToggleAlarmSoundPayload(BlockPos bePos, boolean shouldPlay, int soundX, int soundY, int soundZ, float volume, long seed) {
	public static final ResourceLocation CHANNEL = SCContent.id("toggle_alarm_sound");

	public static ToggleAlarmSoundPayload off(BlockPos bePos) {
		return new ToggleAlarmSoundPayload(bePos, false, (int) (bePos.getX() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getY() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getZ() * ClientboundSoundPacket.LOCATION_ACCURACY), 0.0F, 0L);
	}

	public static ToggleAlarmSoundPayload on(BlockPos bePos, float volume, long seed) {
		return new ToggleAlarmSoundPayload(bePos, true, (int) (bePos.getX() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getY() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getZ() * ClientboundSoundPacket.LOCATION_ACCURACY), volume, seed);
	}

	public double getX() {
		return soundX / ClientboundSoundPacket.LOCATION_ACCURACY;
	}

	public double getY() {
		return soundY / ClientboundSoundPacket.LOCATION_ACCURACY;
	}

	public double getZ() {
		return soundZ / ClientboundSoundPacket.LOCATION_ACCURACY;
	}

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(bePos);
		buf.writeBoolean(shouldPlay);
		buf.writeVarInt(soundX);
		buf.writeVarInt(soundY);
		buf.writeVarInt(soundZ);
		buf.writeFloat(volume);
		buf.writeVarLong(seed);
		return buf;
	}

	public static ToggleAlarmSoundPayload read(FriendlyByteBuf buf) {
		return new ToggleAlarmSoundPayload(buf.readBlockPos(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readVarLong());
	}
}
