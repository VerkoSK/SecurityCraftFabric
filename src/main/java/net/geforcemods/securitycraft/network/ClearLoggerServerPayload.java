package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClearLoggerServerPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<ClearLoggerServerPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "clear_logger_server"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClearLoggerServerPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ClearLoggerServerPayload::pos,
			ClearLoggerServerPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
