package net.geforcemods.securitycraft.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: unbind the mine at the given position from the mine remote access tool the player is holding. 1:1 with upstream RemoveMineFromMRAT. */
public record RemoveMineFromMRATPayload(GlobalPos globalPos) implements CustomPacketPayload {
	public static final Type<RemoveMineFromMRATPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "remove_mine_from_mrat"));
	//@formatter:off
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMineFromMRATPayload> CODEC = StreamCodec.composite(
			GlobalPos.STREAM_CODEC, RemoveMineFromMRATPayload::globalPos,
			RemoveMineFromMRATPayload::new);
	//@formatter:on

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
