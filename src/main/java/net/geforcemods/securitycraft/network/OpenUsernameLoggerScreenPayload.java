package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenUsernameLoggerScreenPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<OpenUsernameLoggerScreenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "open_username_logger_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenUsernameLoggerScreenPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenUsernameLoggerScreenPayload::pos,
			OpenUsernameLoggerScreenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
