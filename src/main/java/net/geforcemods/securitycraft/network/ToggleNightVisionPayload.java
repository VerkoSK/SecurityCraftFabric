package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ToggleNightVisionPayload() {
	public static final ResourceLocation CHANNEL = SCContent.id("toggle_night_vision");

	public FriendlyByteBuf write() {
		return PacketByteBufs.create();
	}

	public static ToggleNightVisionPayload read(FriendlyByteBuf buf) {
		return new ToggleNightVisionPayload();
	}
}
