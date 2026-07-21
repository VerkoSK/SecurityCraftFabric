package net.geforcemods.securitycraft.inventory;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Extended screen-handler data for the laser block: position + per-side enabled config. */
public record LaserBlockData(BlockPos pos, Map<Direction, Boolean> sideConfig) {
	public static final StreamCodec<RegistryFriendlyByteBuf, LaserBlockData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
		buf.writeBlockPos(data.pos());

		int mask = 0;

		for (Direction dir : Direction.values()) {
			if (data.sideConfig().getOrDefault(dir, true))
				mask |= 1 << dir.ordinal();
		}

		buf.writeByte(mask);
	}, buf -> {
		BlockPos pos = buf.readBlockPos();
		int mask = buf.readByte();
		EnumMap<Direction, Boolean> sideConfig = new EnumMap<>(Direction.class);

		for (Direction dir : Direction.values()) {
			sideConfig.put(dir, (mask & 1 << dir.ordinal()) != 0);
		}

		return new LaserBlockData(pos, sideConfig);
	});
}
