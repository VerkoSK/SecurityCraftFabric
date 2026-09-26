package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: set the remaining uses on the limited-use keycard currently in a reader/lock's slot. */
public record SetKeycardUsesPayload(BlockPos pos, int usesLeft) implements CustomPacketPayload {
	public static final Type<SetKeycardUsesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "set_keycard_uses"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetKeycardUsesPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SetKeycardUsesPayload::pos,
			ByteBufCodecs.VAR_INT, SetKeycardUsesPayload::usesLeft,
			SetKeycardUsesPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
