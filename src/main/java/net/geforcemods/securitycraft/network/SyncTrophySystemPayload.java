package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

public record SyncTrophySystemPayload(BlockPos pos, EntityType<?> projectileType, boolean allowed) implements SCPayload {
	public static final PacketType<SyncTrophySystemPayload> TYPE = PacketType.create(SCContent.id("sync_trophy_system"), SyncTrophySystemPayload::new);

	public SyncTrophySystemPayload(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation()), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeResourceLocation(Utils.getRegistryName(projectileType));
		buf.writeBoolean(allowed);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
