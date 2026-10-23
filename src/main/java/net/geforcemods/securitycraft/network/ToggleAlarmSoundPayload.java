package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.blockentities.AlarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record ToggleAlarmSoundPayload(BlockPos bePos, boolean shouldPlay, int soundX, int soundY, int soundZ, float volume, long seed) implements CustomPacketPayload {
	public static final Type<ToggleAlarmSoundPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "toggle_alarm_sound"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAlarmSoundPayload> CODEC = StreamCodec.of(
			(buf, val) -> {
				BlockPos.STREAM_CODEC.encode(buf, val.bePos);
				buf.writeBoolean(val.shouldPlay);
				buf.writeVarInt(val.soundX);
				buf.writeVarInt(val.soundY);
				buf.writeVarInt(val.soundZ);
				buf.writeFloat(val.volume);
				buf.writeVarLong(val.seed);
			},
			buf -> new ToggleAlarmSoundPayload(
					BlockPos.STREAM_CODEC.decode(buf),
					buf.readBoolean(),
					buf.readVarInt(),
					buf.readVarInt(),
					buf.readVarInt(),
					buf.readFloat(),
					buf.readVarLong()
			)
	);

	private ToggleAlarmSoundPayload(BlockPos bePos, boolean shouldPlay, float volume, long seed) {
		this(bePos, shouldPlay, (int) (bePos.getX() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getY() * ClientboundSoundPacket.LOCATION_ACCURACY), (int) (bePos.getZ() * ClientboundSoundPacket.LOCATION_ACCURACY), volume, seed);
	}

	public static ToggleAlarmSoundPayload off(BlockPos bePos) {
		return new ToggleAlarmSoundPayload(bePos, false, 0.0F, 0);
	}

	public static ToggleAlarmSoundPayload on(BlockPos bePos, float volume, long seed) {
		return new ToggleAlarmSoundPayload(bePos, true, volume, seed);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
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

	public void handle(Level level) {
		if (level != null && level.getBlockEntity(bePos) instanceof AlarmBlockEntity be) {
			if (shouldPlay)
				be.playSound(level, getX(), getY(), getZ(), volume, seed);
			else
				be.stopPlayingSound();
		}
	}
}
