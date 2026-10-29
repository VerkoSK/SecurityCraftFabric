package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record SyncRiftStabilizerPayload(BlockPos pos, TeleportationType teleportationType, boolean allowed) implements SCPayload {
	public static final PacketType<SyncRiftStabilizerPayload> TYPE = PacketType.create(SCContent.id("sync_rift_stabilizer"), SyncRiftStabilizerPayload::new);

	public SyncRiftStabilizerPayload(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), buf.readEnum(TeleportationType.class), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeEnum(teleportationType);
		buf.writeBoolean(allowed);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
