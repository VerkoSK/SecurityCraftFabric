package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: toggle the held Lvl2/Lvl3 reinforcer between reinforcing and unreinforcing. */
public record SyncBlockReinforcerPayload(boolean isReinforcing) implements CustomPacketPayload {
	public static final Type<SyncBlockReinforcerPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "sync_block_reinforcer"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncBlockReinforcerPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SyncBlockReinforcerPayload::isReinforcing,
			SyncBlockReinforcerPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
