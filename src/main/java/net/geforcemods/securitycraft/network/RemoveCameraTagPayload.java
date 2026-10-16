package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RemoveCameraTagPayload(int camId) {
	public static final ResourceLocation CHANNEL = SCContent.id("remove_camera_tag");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeInt(camId);
		return buf;
	}

	public static RemoveCameraTagPayload read(FriendlyByteBuf buf) {
		return new RemoveCameraTagPayload(buf.readInt());
	}
}
