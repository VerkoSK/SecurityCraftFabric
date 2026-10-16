package net.geforcemods.securitycraft.network;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record UpdateLoggerPayload(BlockPos pos, int index, String username, UUID uuid, long timestamp) {
	public static final ResourceLocation CHANNEL = SCContent.id("update_logger");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeVarInt(index);
		buf.writeUtf(username);
		buf.writeUUID(uuid);
		buf.writeVarLong(timestamp);
		return buf;
	}

	public static UpdateLoggerPayload read(FriendlyByteBuf buf) {
		return new UpdateLoggerPayload(
				buf.readBlockPos(),
				buf.readVarInt(),
				buf.readUtf(Integer.MAX_VALUE / 4),
				buf.readUUID(),
				buf.readVarLong()
		);
	}
}
