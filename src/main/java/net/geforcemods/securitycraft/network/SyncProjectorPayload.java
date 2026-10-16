package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record SyncProjectorPayload(BlockPos pos, int data, DataType dataType) {
	public static final ResourceLocation CHANNEL = SCContent.id("sync_projector");

	public SyncProjectorPayload(BlockPos pos, BlockState state) {
		this(pos, Block.getId(state), DataType.BLOCK_STATE);
	}

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeEnum(dataType);

		if (dataType.isBoolean)
			buf.writeBoolean(data == 1);
		else
			buf.writeVarInt(data);

		return buf;
	}

	public static SyncProjectorPayload read(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		DataType dataType = buf.readEnum(DataType.class);
		int data = dataType.isBoolean ? (buf.readBoolean() ? 1 : 0) : buf.readVarInt();

		return new SyncProjectorPayload(pos, data, dataType);
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
