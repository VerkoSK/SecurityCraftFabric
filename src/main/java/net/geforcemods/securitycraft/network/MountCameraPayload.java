package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MountCameraPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<MountCameraPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "mount_camera"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MountCameraPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, MountCameraPayload::pos,
			MountCameraPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
