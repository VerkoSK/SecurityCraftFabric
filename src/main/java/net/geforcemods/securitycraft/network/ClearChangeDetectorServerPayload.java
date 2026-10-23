package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClearChangeDetectorServerPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<ClearChangeDetectorServerPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "clear_change_detector_server"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClearChangeDetectorServerPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ClearChangeDetectorServerPayload::pos,
			ClearChangeDetectorServerPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
