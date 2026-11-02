package net.geforcemods.securitycraft.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RemoveCameraTagPayload(GlobalPos globalPos) implements CustomPacketPayload {
	public static final Type<RemoveCameraTagPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "remove_camera_tag"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveCameraTagPayload> CODEC = StreamCodec.composite(
			GlobalPos.STREAM_CODEC, RemoveCameraTagPayload::globalPos,
			RemoveCameraTagPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
