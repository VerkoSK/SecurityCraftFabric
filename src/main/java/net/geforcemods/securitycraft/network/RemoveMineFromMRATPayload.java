package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: unbind the mine in slot {@code mineIndex} (0-5) from the mine remote access tool the player is holding. 1:1 with upstream RemoveMineFromMRAT. */
public record RemoveMineFromMRATPayload(int mineIndex) implements CustomPacketPayload {
	public static final Type<RemoveMineFromMRATPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "remove_mine_from_mrat"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMineFromMRATPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, RemoveMineFromMRATPayload::mineIndex,
			RemoveMineFromMRATPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
