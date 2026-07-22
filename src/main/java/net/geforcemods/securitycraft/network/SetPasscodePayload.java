package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: the owner sets a new passcode on the keypad at {@code pos}. */
public record SetPasscodePayload(BlockPos pos, String passcode) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "set_passcode");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeUtf(passcode);
		return buf;
	}

	public static SetPasscodePayload read(FriendlyByteBuf buf) {
		return new SetPasscodePayload(buf.readBlockPos(), buf.readUtf());
	}
}
