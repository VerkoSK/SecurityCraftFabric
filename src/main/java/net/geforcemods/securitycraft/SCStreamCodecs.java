package net.geforcemods.securitycraft;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SCStreamCodecs {
	public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.VAR_INT.map(Block::stateById, Block::getId);
	public static final StreamCodec<ByteBuf, StandingOrWallType> STANDING_OR_WALL_TYPE = ByteBufCodecs.idMapper(i -> StandingOrWallType.values()[i], StandingOrWallType::ordinal);
	public static final StreamCodec<FriendlyByteBuf, boolean[]> BOOLEAN_ARRAY = new StreamCodec<>() {
		@Override
		public boolean[] decode(FriendlyByteBuf buf) {
			int size = buf.readVarInt();
			boolean[] array = new boolean[size];

			for (int i = 0; i < size; i++) {
				array[i] = buf.readBoolean();
			}

			return array;
		}

		@Override
		public void encode(FriendlyByteBuf buf, boolean[] array) {
			buf.writeVarInt(array.length);

			for (int i = 0; i < array.length; i++) {
				buf.writeBoolean(array[i]);
			}
		}
	};

	private SCStreamCodecs() {}
}
