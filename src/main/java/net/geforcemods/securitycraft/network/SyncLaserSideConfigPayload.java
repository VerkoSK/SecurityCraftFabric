package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: the owner changes which sides of a laser block emit beams (requires the smart module). */
public record SyncLaserSideConfigPayload(BlockPos pos, CompoundTag sideConfig) implements CustomPacketPayload {
	public static final Type<SyncLaserSideConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "sync_laser_side_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncLaserSideConfigPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncLaserSideConfigPayload::pos,
			ByteBufCodecs.COMPOUND_TAG, SyncLaserSideConfigPayload::sideConfig,
			SyncLaserSideConfigPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
