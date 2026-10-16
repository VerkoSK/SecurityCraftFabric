package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SetDefaultCameraViewingDirectionPayload(int id, float xRot, float yRot, float zoom) {
	public static final ResourceLocation CHANNEL = SCContent.id("set_default_camera_viewing_direction");

	public SetDefaultCameraViewingDirectionPayload(SecurityCamera cam) {
		this(cam.getId(), cam.getXRot(), cam.getYRot(), cam.getZoomAmount());
	}

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(id);
		buf.writeFloat(xRot);
		buf.writeFloat(yRot);
		buf.writeFloat(zoom);
		return buf;
	}

	public static SetDefaultCameraViewingDirectionPayload read(FriendlyByteBuf buf) {
		return new SetDefaultCameraViewingDirectionPayload(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
	}
}
