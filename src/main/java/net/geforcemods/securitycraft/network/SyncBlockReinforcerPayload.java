package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: toggle the held Lvl2/Lvl3 reinforcer between reinforcing and unreinforcing. */
public record SyncBlockReinforcerPayload(boolean isReinforcing) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "sync_block_reinforcer");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBoolean(isReinforcing);
		return buf;
	}

	public static SyncBlockReinforcerPayload read(FriendlyByteBuf buf) {
		return new SyncBlockReinforcerPayload(buf.readBoolean());
	}
}
