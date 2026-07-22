package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: the owner changes which sides of a laser block emit beams (requires the smart module). */
public record SyncLaserSideConfigPayload(BlockPos pos, CompoundTag sideConfig) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "sync_laser_side_config");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeNbt(sideConfig);
		return buf;
	}

	public static SyncLaserSideConfigPayload read(FriendlyByteBuf buf) {
		return new SyncLaserSideConfigPayload(buf.readBlockPos(), buf.readNbt());
	}
}
