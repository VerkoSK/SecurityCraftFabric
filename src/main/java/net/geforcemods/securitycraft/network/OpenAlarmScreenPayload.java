package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenAlarmScreenPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<OpenAlarmScreenPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "open_alarm_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenAlarmScreenPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenAlarmScreenPayload::pos,
			OpenAlarmScreenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
