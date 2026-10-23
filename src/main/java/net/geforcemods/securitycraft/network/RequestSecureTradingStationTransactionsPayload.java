package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestSecureTradingStationTransactionsPayload(BlockPos pos, int requestedTransactions) implements CustomPacketPayload {
	public static final Type<RequestSecureTradingStationTransactionsPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "request_secure_trading_station_transactions"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestSecureTradingStationTransactionsPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, RequestSecureTradingStationTransactionsPayload::pos,
			ByteBufCodecs.VAR_INT, RequestSecureTradingStationTransactionsPayload::requestedTransactions,
			RequestSecureTradingStationTransactionsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
