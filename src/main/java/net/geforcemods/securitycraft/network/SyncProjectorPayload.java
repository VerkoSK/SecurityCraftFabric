package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record SyncProjectorPayload(BlockPos pos, DataType dataType, int data) implements CustomPacketPayload {
	public static final Type<SyncProjectorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "sync_projector"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncProjectorPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncProjectorPayload::pos,
			ByteBufCodecs.idMapper(i -> DataType.values()[i], DataType::ordinal), SyncProjectorPayload::dataType,
			ByteBufCodecs.VAR_INT, SyncProjectorPayload::data,
			SyncProjectorPayload::new);

	public SyncProjectorPayload(BlockPos pos, BlockState state) {
		this(pos, DataType.BLOCK_STATE, Block.getId(state));
	}

	public SyncProjectorPayload(BlockPos pos, int data, DataType dataType) {
		this(pos, dataType, data);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public enum DataType {
		WIDTH,
		HEIGHT,
		RANGE,
		OFFSET,
		HORIZONTAL(true),
		OVERRIDING_BLOCKS(true),
		BLOCK_STATE,
		INVALID;

		public final boolean isBoolean;

		DataType() {
			this(false);
		}

		DataType(boolean isBoolean) {
			this.isBoolean = isBoolean;
		}
	}
}
