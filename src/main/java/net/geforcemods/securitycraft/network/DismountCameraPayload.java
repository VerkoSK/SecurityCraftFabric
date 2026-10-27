package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DismountCameraPayload() implements CustomPacketPayload {
	public static final Type<DismountCameraPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "dismount_camera"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DismountCameraPayload> CODEC = StreamCodec.unit(new DismountCameraPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
