package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClearChangeDetectorServerPayload(BlockPos pos) {
	public static final ResourceLocation CHANNEL = SCContent.id("clear_change_detector_server");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		return buf;
	}

	public static ClearChangeDetectorServerPayload read(FriendlyByteBuf buf) {
		return new ClearChangeDetectorServerPayload(buf.readBlockPos());
	}
}
