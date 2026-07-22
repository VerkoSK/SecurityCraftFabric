package net.geforcemods.securitycraft.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: re-render the laser field blocks at the given positions so an updated lens colour shows. */
public record UpdateLaserColorsPayload(List<BlockPos> positions) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "update_laser_colors");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeCollection(positions, FriendlyByteBuf::writeBlockPos);
		return buf;
	}

	public static UpdateLaserColorsPayload read(FriendlyByteBuf buf) {
		return new UpdateLaserColorsPayload(buf.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos));
	}
}
