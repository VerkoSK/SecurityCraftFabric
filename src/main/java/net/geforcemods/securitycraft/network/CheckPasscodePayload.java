package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: attempt a passcode on the keypad at {@code pos}. */
public record CheckPasscodePayload(BlockPos pos, String passcode) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "check_passcode");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeUtf(passcode);
		return buf;
	}

	public static CheckPasscodePayload read(FriendlyByteBuf buf) {
		return new CheckPasscodePayload(buf.readBlockPos(), buf.readUtf());
	}
}
