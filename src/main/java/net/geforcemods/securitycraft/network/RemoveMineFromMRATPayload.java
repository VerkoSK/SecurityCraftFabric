package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: unbind the mine in slot {@code mineIndex} (1-6) from the mine remote access tool the player is holding. 1:1 with upstream RemoveMineFromMRAT. */
public record RemoveMineFromMRATPayload(int mineIndex) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "remove_mine_from_mrat");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(mineIndex);
		return buf;
	}

	public static RemoveMineFromMRATPayload read(FriendlyByteBuf buf) {
		return new RemoveMineFromMRATPayload(buf.readVarInt());
	}
}
