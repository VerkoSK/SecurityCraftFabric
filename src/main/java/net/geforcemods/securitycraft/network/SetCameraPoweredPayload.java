package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SetCameraPoweredPayload(BlockPos pos, boolean powered) {
	public static final ResourceLocation CHANNEL = SCContent.id("set_camera_powered");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeBoolean(powered);
		return buf;
	}

	public static SetCameraPoweredPayload read(FriendlyByteBuf buf) {
		return new SetCameraPoweredPayload(buf.readBlockPos(), buf.readBoolean());
	}
}
