package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleNightVisionPayload() implements CustomPacketPayload {
	public static final Type<ToggleNightVisionPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "toggle_night_vision"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleNightVisionPayload> CODEC = StreamCodec.unit(new ToggleNightVisionPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
