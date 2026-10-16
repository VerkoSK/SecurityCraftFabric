package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RequestSecureTradingStationTransactionsPayload(BlockPos pos, int requestedTransactions) {
	public static final ResourceLocation CHANNEL = SCContent.id("request_sts_transactions");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBlockPos(pos);
		buf.writeVarInt(requestedTransactions);
		return buf;
	}

	public static RequestSecureTradingStationTransactionsPayload read(FriendlyByteBuf buf) {
		return new RequestSecureTradingStationTransactionsPayload(buf.readBlockPos(), buf.readVarInt());
	}
}
