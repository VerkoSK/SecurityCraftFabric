package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: change one of a block's options. A toggle flips booleans and cycles enums; numeric options
 * send the value the slider landed on instead. 1:1 in intent with upstream's ToggleOption packet.
 */
public record SetOptionPayload(BlockPos pos, int optionIndex, boolean toggle, double value) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "set_option");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeVarInt(optionIndex);
		buf.writeBoolean(toggle);
		buf.writeDouble(value);
		return buf;
	}

	public static SetOptionPayload read(FriendlyByteBuf buf) {
		return new SetOptionPayload(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean(), buf.readDouble());
	}
}
