package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record SetTrophySystemTargetPayload(BlockPos pos, int targetID) implements SCPayload {
	public static final PacketType<SetTrophySystemTargetPayload> TYPE = PacketType.create(SCContent.id("set_trophy_system_target"), SetTrophySystemTargetPayload::new);

	public SetTrophySystemTargetPayload(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), buf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeVarInt(targetID);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
