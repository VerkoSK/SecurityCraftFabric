package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The mine remote access tool's bound mine positions (up to {@link #MAX_MINES} slots, {@code null} = empty slot).
 * Fabric equivalent of upstream's {@code GlobalPositions} component, simplified to a single dimension (this port's
 * mines are always in the level the tool is used in, so a full {@code GlobalPos} per slot is not needed).
 */
public record BoundMines(List<BlockPos> positions) {
	public static final int MAX_MINES = 6;
	public static final BoundMines EMPTY = new BoundMines(nullList());
	//@formatter:off
	public static final Codec<BoundMines> CODEC = BlockPos.CODEC.listOf().xmap(BoundMines::new, BoundMines::positions);
	public static final StreamCodec<io.netty.buffer.ByteBuf, BoundMines> STREAM_CODEC = ByteBufCodecs.optional(BlockPos.STREAM_CODEC).apply(ByteBufCodecs.list(MAX_MINES)).map(list -> new BoundMines(list.stream().map(o -> o.orElse(null)).toList()), bm -> bm.positions().stream().map(java.util.Optional::ofNullable).toList());
	//@formatter:on

	private static List<BlockPos> nullList() {
		List<BlockPos> list = new ArrayList<>(MAX_MINES);

		for (int i = 0; i < MAX_MINES; i++) {
			list.add(null);
		}

		return list;
	}

	public boolean isEmpty() {
		return positions.stream().allMatch(java.util.Objects::isNull);
	}

	public boolean contains(BlockPos pos) {
		return positions.contains(pos);
	}

	/** @return the 0-based slot index, or -1 if there is no free slot. */
	public int getNextAvailableSlot() {
		return positions.indexOf(null);
	}

	public BoundMines with(BlockPos pos) {
		int slot = getNextAvailableSlot();

		if (slot == -1)
			return this;

		List<BlockPos> copy = new ArrayList<>(positions);

		copy.set(slot, pos);
		return new BoundMines(copy);
	}

	public BoundMines without(BlockPos pos) {
		int slot = positions.indexOf(pos);

		if (slot == -1)
			return this;

		return withoutSlot(slot);
	}

	public BoundMines withoutSlot(int slot) {
		if (slot < 0 || slot >= positions.size())
			return this;

		List<BlockPos> copy = new ArrayList<>(positions);

		copy.set(slot, null);
		return new BoundMines(copy);
	}
}
