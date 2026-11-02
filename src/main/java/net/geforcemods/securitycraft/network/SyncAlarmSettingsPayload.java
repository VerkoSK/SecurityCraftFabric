package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncAlarmSettingsPayload(BlockPos pos, Identifier soundEvent, float pitch, int soundLength) implements CustomPacketPayload {
	public static final Type<SyncAlarmSettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "sync_alarm_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncAlarmSettingsPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncAlarmSettingsPayload::pos,
			Identifier.STREAM_CODEC, SyncAlarmSettingsPayload::soundEvent,
			ByteBufCodecs.FLOAT, SyncAlarmSettingsPayload::pitch,
			ByteBufCodecs.VAR_INT, SyncAlarmSettingsPayload::soundLength,
			SyncAlarmSettingsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
