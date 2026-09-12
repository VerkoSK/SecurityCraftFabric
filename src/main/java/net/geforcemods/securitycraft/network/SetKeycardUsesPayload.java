package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: set the remaining uses on the limited-use keycard currently in a reader/lock's slot. */
public record SetKeycardUsesPayload(BlockPos pos, int usesLeft) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "set_keycard_uses");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeVarInt(usesLeft);
		return buf;
	}

	public static SetKeycardUsesPayload read(FriendlyByteBuf buf) {
		return new SetKeycardUsesPayload(buf.readBlockPos(), buf.readVarInt());
	}
}
