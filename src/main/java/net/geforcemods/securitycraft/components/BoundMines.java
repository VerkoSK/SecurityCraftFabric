package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * The mine remote access tool's bound-mine slots, stored as a data component.
 *
 * <p>Upstream's {@code GlobalPositions} keeps a fixed-size list whose empty slots are literal nulls, which its codecs
 * translate to/from an empty map (persistent) and a dummy {@link GlobalPos} (network). This port keeps the same
 * fixed-size, slot-stable shape but uses the dummy position on both sides, so the codecs stay plain vanilla ones.
 */
public record BoundMines(List<GlobalPos> positions) {
	public static final int MAX_MINES = 6;
	/** Marks a slot as empty. Its dimension key cannot collide with a real one, so it can never match a bound mine. */
	public static final GlobalPos EMPTY_SLOT = new GlobalPos(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("securitycraft", "empty")), BlockPos.ZERO);
	public static final BoundMines EMPTY = new BoundMines(Collections.nCopies(MAX_MINES, EMPTY_SLOT));
	public static final Codec<BoundMines> CODEC = GlobalPos.CODEC.listOf().xmap(BoundMines::sized, BoundMines::positions);
	public static final StreamCodec<ByteBuf, BoundMines> STREAM_CODEC = GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_MINES)).map(BoundMines::sized, BoundMines::positions);

	/** Pads or trims a decoded list to {@link #MAX_MINES} entries, so a hand-written or outdated component cannot desync the slots. */
	private static BoundMines sized(List<GlobalPos> decoded) {
		List<GlobalPos> slots = new ArrayList<>(MAX_MINES);

		for (int i = 0; i < MAX_MINES; i++) {
			slots.add(i < decoded.size() ? decoded.get(i) : EMPTY_SLOT);
		}

		return new BoundMines(slots);
	}

	public boolean isEmpty() {
		return positions.stream().allMatch(EMPTY_SLOT::equals);
	}

	/** @return the position bound in the given slot, or null if that slot is empty */
	public GlobalPos get(int slot) {
		GlobalPos globalPos = positions.get(slot);

		return EMPTY_SLOT.equals(globalPos) ? null : globalPos;
	}

	public boolean contains(GlobalPos globalPos) {
		return globalPos != null && positions.contains(globalPos);
	}

	/** @return the slot the given position sits in, or -1 if it is not bound */
	public int indexOf(GlobalPos globalPos) {
		return globalPos == null ? -1 : positions.indexOf(globalPos);
	}

	/** @return the first free slot, or -1 if all six are taken */
	public int firstFreeSlot() {
		return positions.indexOf(EMPTY_SLOT);
	}

	public BoundMines with(int slot, GlobalPos globalPos) {
		List<GlobalPos> newPositions = new ArrayList<>(positions);

		newPositions.set(slot, globalPos == null ? EMPTY_SLOT : globalPos);
		return new BoundMines(newPositions);
	}
}
