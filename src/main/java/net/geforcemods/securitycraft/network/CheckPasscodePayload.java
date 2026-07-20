package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: attempt a passcode on the keypad at {@code pos}. */
public record CheckPasscodePayload(BlockPos pos, String passcode) implements CustomPacketPayload {
	public static final Type<CheckPasscodePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "check_passcode"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CheckPasscodePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, CheckPasscodePayload::pos,
			ByteBufCodecs.STRING_UTF8, CheckPasscodePayload::passcode,
			CheckPasscodePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
