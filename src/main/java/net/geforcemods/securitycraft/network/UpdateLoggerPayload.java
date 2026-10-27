package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateLoggerPayload(BlockPos pos, int index, String player, String uuid, long timestamp) implements CustomPacketPayload {
	public static final Type<UpdateLoggerPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "update_logger"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLoggerPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, UpdateLoggerPayload::pos,
			ByteBufCodecs.VAR_INT, UpdateLoggerPayload::index,
			ByteBufCodecs.STRING_UTF8, UpdateLoggerPayload::player,
			ByteBufCodecs.STRING_UTF8, UpdateLoggerPayload::uuid,
			ByteBufCodecs.VAR_LONG, UpdateLoggerPayload::timestamp,
			UpdateLoggerPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
