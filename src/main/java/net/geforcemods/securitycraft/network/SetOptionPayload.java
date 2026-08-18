package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: change one of a block's options. A toggle flips booleans and cycles enums; numeric options
 * send the value the slider landed on instead. 1:1 in intent with upstream's ToggleOption packet.
 */
public record SetOptionPayload(BlockPos pos, int optionIndex, boolean toggle, double value) implements CustomPacketPayload {
	public static final Type<SetOptionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "set_option"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetOptionPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SetOptionPayload::pos,
			ByteBufCodecs.VAR_INT, SetOptionPayload::optionIndex,
			ByteBufCodecs.BOOL, SetOptionPayload::toggle,
			ByteBufCodecs.DOUBLE, SetOptionPayload::value,
			SetOptionPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
