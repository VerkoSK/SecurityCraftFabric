package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetCameraPoweredPayload(BlockPos pos, boolean powered) implements CustomPacketPayload {
	public static final Type<SetCameraPoweredPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "set_camera_powered"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetCameraPoweredPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SetCameraPoweredPayload::pos,
			ByteBufCodecs.BOOL, SetCameraPoweredPayload::powered,
			SetCameraPoweredPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
