package net.geforcemods.securitycraft.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -&gt; server: unbind the given mine from the mine remote access tool the player is holding. 1:1 with upstream RemoveMineFromMRAT. */
public record RemoveMineFromMRATPayload(GlobalPos globalPos) implements CustomPacketPayload {
	public static final Type<RemoveMineFromMRATPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "remove_mine_from_mrat"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMineFromMRATPayload> CODEC = StreamCodec.composite(
			GlobalPos.STREAM_CODEC, RemoveMineFromMRATPayload::globalPos,
			RemoveMineFromMRATPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
