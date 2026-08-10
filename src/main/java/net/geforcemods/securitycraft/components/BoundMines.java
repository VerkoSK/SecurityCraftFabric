package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The mines bound to a mine remote access tool. Fabric equivalent of upstream's {@code components.GlobalPositions},
 * reduced to the single use this port has: upstream keeps null holes so a mine always stays in the slot it was bound
 * to, this port stores the bound mines compactly instead, so unbinding one shifts the ones below it up.
 */
public record BoundMines(List<GlobalPos> positions) {
	public static final int MAX_MINES = 6;
	public static final BoundMines EMPTY = new BoundMines(List.of());
	public static final Codec<BoundMines> CODEC = GlobalPos.CODEC.listOf().xmap(BoundMines::new, BoundMines::positions);
	public static final StreamCodec<ByteBuf, BoundMines> STREAM_CODEC = ByteBufCodecs.<ByteBuf, GlobalPos>list(MAX_MINES).apply(GlobalPos.STREAM_CODEC).map(BoundMines::new, BoundMines::positions);

	public boolean contains(GlobalPos pos) {
		return positions.contains(pos);
	}

	public boolean isFull() {
		return positions.size() >= MAX_MINES;
	}

	/** @return The mine bound in the given (0 based) slot, or null if that slot is empty */
	public GlobalPos get(int slot) {
		return slot >= 0 && slot < positions.size() ? positions.get(slot) : null;
	}

	public BoundMines with(GlobalPos pos) {
		List<GlobalPos> newPositions = new ArrayList<>(positions);

		newPositions.add(pos);
		return new BoundMines(List.copyOf(newPositions));
	}

	public BoundMines without(GlobalPos pos) {
		List<GlobalPos> newPositions = new ArrayList<>(positions);

		newPositions.remove(pos);
		return new BoundMines(List.copyOf(newPositions));
	}

	public BoundMines withoutSlot(int slot) {
		if (slot < 0 || slot >= positions.size())
			return this;

		List<GlobalPos> newPositions = new ArrayList<>(positions);

		newPositions.remove(slot);
		return new BoundMines(List.copyOf(newPositions));
	}
}
