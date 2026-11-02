package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RefreshDisguisableModelPayload(BlockPos pos, boolean hasModule) implements CustomPacketPayload {
	public static final Type<RefreshDisguisableModelPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "refresh_disguisable_model"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RefreshDisguisableModelPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, RefreshDisguisableModelPayload::pos,
			ByteBufCodecs.BOOL, RefreshDisguisableModelPayload::hasModule,
			RefreshDisguisableModelPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
