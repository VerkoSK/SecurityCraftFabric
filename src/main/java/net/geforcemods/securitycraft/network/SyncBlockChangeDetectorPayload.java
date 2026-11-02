package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.DetectionMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncBlockChangeDetectorPayload(BlockPos pos, DetectionMode mode, boolean showHighlights, int color) implements CustomPacketPayload {
	public static final Type<SyncBlockChangeDetectorPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "sync_block_change_detector"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncBlockChangeDetectorPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncBlockChangeDetectorPayload::pos,
			ByteBufCodecs.idMapper(i -> DetectionMode.values()[i], DetectionMode::ordinal), SyncBlockChangeDetectorPayload::mode,
			ByteBufCodecs.BOOL, SyncBlockChangeDetectorPayload::showHighlights,
			ByteBufCodecs.VAR_INT, SyncBlockChangeDetectorPayload::color,
			SyncBlockChangeDetectorPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
