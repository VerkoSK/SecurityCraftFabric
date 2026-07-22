package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: open the keypad screen at {@code pos} in setup or entry mode. */
public record OpenKeypadScreenPayload(BlockPos pos, boolean setup, String ownerName) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "open_keypad_screen");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeBoolean(setup);
		buf.writeUtf(ownerName);
		return buf;
	}

	public static OpenKeypadScreenPayload read(FriendlyByteBuf buf) {
		return new OpenKeypadScreenPayload(buf.readBlockPos(), buf.readBoolean(), buf.readUtf());
	}
}
