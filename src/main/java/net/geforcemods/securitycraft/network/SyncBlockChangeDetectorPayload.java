package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.DetectionMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncBlockChangeDetectorPayload(BlockPos pos, DetectionMode mode, boolean showHighlights, int color) {
	public static final ResourceLocation CHANNEL = SCContent.id("sync_block_change_detector");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeEnum(mode);
		buf.writeBoolean(showHighlights);
		buf.writeInt(color);
		return buf;
	}

	public static SyncBlockChangeDetectorPayload read(FriendlyByteBuf buf) {
		return new SyncBlockChangeDetectorPayload(buf.readBlockPos(), buf.readEnum(DetectionMode.class), buf.readBoolean(), buf.readInt());
	}
}
