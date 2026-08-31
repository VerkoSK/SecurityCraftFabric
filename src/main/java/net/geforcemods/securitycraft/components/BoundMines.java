package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The mine remote access tool's bound mine positions (always {@link #MAX_MINES} slots, {@code null} = empty slot).
 * Fabric equivalent of upstream's {@code GlobalPositions} component, simplified to a single dimension (this port's
 * mines are always in the level the tool is used in, so a full {@code GlobalPos} per slot is not needed).
 */
public record BoundMines(List<BlockPos> positions) {
	public static final int MAX_MINES = 6;
	public static final BoundMines EMPTY = new BoundMines(new ArrayList<>());
	//@formatter:off
	public static final Codec<BoundMines> CODEC = BlockPos.CODEC.optionalFieldOf("pos").codec().listOf().xmap(
			slots -> new BoundMines(slots.stream().map(slot -> slot.orElse(null)).collect(Collectors.toCollection(ArrayList::new))),
			mines -> mines.positions().stream().map(Optional::ofNullable).toList());
	public static final StreamCodec<io.netty.buffer.ByteBuf, BoundMines> STREAM_CODEC = ByteBufCodecs.optional(BlockPos.STREAM_CODEC).apply(ByteBufCodecs.list(MAX_MINES)).map(
			slots -> new BoundMines(slots.stream().map(slot -> slot.orElse(null)).collect(Collectors.toCollection(ArrayList::new))),
			mines -> mines.positions().stream().map(Optional::ofNullable).toList());
	//@formatter:on

	/**
	 * Forces the slot list to exactly {@link #MAX_MINES} entries by padding with {@code null} or dropping the overflow.
	 * Normal play always produces this length, but a hand-written component, a data pack or a world carried over from an
	 * older build can hand back a different count; without the fixed length the codecs above would either drop null
	 * slots (the old {@code BlockPos.CODEC.listOf()} could not encode them at all) or desync the network list size,
	 * which made {@code ItemStack} re-encoding throw and dropped the connection on the next inventory packet.
	 */
	public BoundMines {
		if (positions.size() != MAX_MINES) {
			List<BlockPos> fixed = new ArrayList<>(MAX_MINES);

			for (int i = 0; i < MAX_MINES; i++)
				fixed.add(i < positions.size() ? positions.get(i) : null);

			positions = fixed;
		}
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
