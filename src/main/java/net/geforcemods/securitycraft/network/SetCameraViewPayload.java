package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SetCameraViewPayload(int entityId) {
	public static final ResourceLocation CHANNEL = SCContent.id("set_camera_view");

	public SetCameraViewPayload(Entity camera) {
		this(camera.getId());
	}

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(entityId);
		return buf;
	}

	public static SetCameraViewPayload read(FriendlyByteBuf buf) {
		return new SetCameraViewPayload(buf.readVarInt());
	}
}
