package net.geforcemods.securitycraft.network;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: re-render the laser field blocks at the given positions so an updated lens colour shows. */
public record UpdateLaserColorsPayload(List<BlockPos> positions) implements CustomPacketPayload {
	public static final Type<UpdateLaserColorsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "update_laser_colors"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLaserColorsPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateLaserColorsPayload::positions,
			UpdateLaserColorsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
