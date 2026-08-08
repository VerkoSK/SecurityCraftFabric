package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * The mine positions bound to a mine remote access tool, stored as the {@code securitycraft:bound_mines} data
 * component. Equivalent to upstream's {@code GlobalPositions}: a fixed-size list of slots, each either holding a
 * position or empty, so unbinding one mine leaves the others in their rows. Upstream keeps the empty slots as nulls
 * behind a custom nullable list codec; an {@link Optional} per slot expresses the same thing with vanilla codecs.
 */
public record BoundMines(List<Optional<GlobalPos>> positions) {
	public static final int MAX_MINES = 6;
	public static final BoundMines EMPTY = new BoundMines(Arrays.asList(new Optional[] {
			Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
	}));
	private static final Codec<Optional<GlobalPos>> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(GlobalPos.CODEC.optionalFieldOf("pos").forGetter(Function.identity())).apply(instance, Function.identity()));
	public static final Codec<BoundMines> CODEC = RecordCodecBuilder.create(instance -> instance.group(SLOT_CODEC.sizeLimitedListOf(MAX_MINES).fieldOf("positions").forGetter(BoundMines::positions)).apply(instance, BoundMines::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BoundMines> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(GlobalPos.STREAM_CODEC).apply(ByteBufCodecs.list(MAX_MINES)), BoundMines::positions,
			BoundMines::new);

	public boolean isEmpty() {
		return positions.stream().allMatch(Optional::isEmpty);
	}

	public boolean contains(GlobalPos globalPos) {
		return positions.contains(Optional.of(globalPos));
	}

	/** @return the position bound in the given slot, or null if the slot is empty or out of range */
	public GlobalPos get(int slot) {
		return slot >= 0 && slot < positions.size() ? positions.get(slot).orElse(null) : null;
	}

	/** Binds the position to the first free slot and writes the result back onto the stack. */
	public boolean add(ItemStack stack, GlobalPos globalPos) {
		if (contains(globalPos))
			return false;

		List<Optional<GlobalPos>> newPositions = new ArrayList<>(positions);

		for (int i = 0; i < newPositions.size(); i++) {
			if (newPositions.get(i).isEmpty()) {
				newPositions.set(i, Optional.of(globalPos));
				stack.set(SCContent.BOUND_MINES, new BoundMines(newPositions));
				return true;
			}
		}

		return false;
	}

	/** Clears the slot holding the given position and writes the result back onto the stack. */
	public boolean remove(ItemStack stack, GlobalPos globalPos) {
		if (globalPos == null)
			return false;

		List<Optional<GlobalPos>> newPositions = new ArrayList<>(positions);
		int index = newPositions.indexOf(Optional.of(globalPos));

		if (index == -1)
			return false;

		newPositions.set(index, Optional.empty());
		stack.set(SCContent.BOUND_MINES, new BoundMines(newPositions));
		return true;
	}
}
