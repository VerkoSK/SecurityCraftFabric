package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: the owner sets a new passcode on the keypad at {@code pos}. */
public record SetPasscodePayload(BlockPos pos, String passcode) implements CustomPacketPayload {
	public static final Type<SetPasscodePayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "set_passcode"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetPasscodePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SetPasscodePayload::pos,
			ByteBufCodecs.STRING_UTF8, SetPasscodePayload::passcode,
			SetPasscodePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
