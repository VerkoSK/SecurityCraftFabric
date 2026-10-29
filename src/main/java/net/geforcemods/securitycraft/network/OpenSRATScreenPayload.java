package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record OpenSRATScreenPayload() {
	public static final ResourceLocation CHANNEL = SCContent.id("open_srat_screen");

	public FriendlyByteBuf write() {
		return PacketByteBufs.create();
	}

	public static OpenSRATScreenPayload read(FriendlyByteBuf buf) {
		return new OpenSRATScreenPayload();
	}
}
