package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DismountCameraPayload() {
	public static final ResourceLocation CHANNEL = SCContent.id("dismount_camera");

	public FriendlyByteBuf write() {
		return PacketByteBufs.create();
	}

	public static DismountCameraPayload read(FriendlyByteBuf buf) {
		return new DismountCameraPayload();
	}
}
