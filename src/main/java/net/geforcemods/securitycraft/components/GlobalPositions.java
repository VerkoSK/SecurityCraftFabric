package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.NullableListCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * A fixed-size list of bound positions, with empty slots stored as nulls. 1:1 with the upstream
 * {@code components.GlobalPositions}, with the upstream {@code GlobalPositionComponent} interface's defaults folded in:
 * the mine remote access tool is the only holder of this component in this port, so the generic interface it shares
 * with the camera monitor and the sentry remote access tool would have no second implementor.
 */
public record GlobalPositions(List<GlobalPos> positions) {
	/** Stand-in for a null entry on the network, where a list element cannot be null. */
	public static final GlobalPos DUMMY_GLOBAL_POS = new GlobalPos(ResourceKey.create(Registries.DIMENSION, SCContent.id("dummy")), BlockPos.ZERO);

	public static Codec<GlobalPositions> codec(int size) {
		return RecordCodecBuilder.create(instance -> instance.group(nullableSizedCodec(GlobalPos.CODEC, size).fieldOf("positions").forGetter(GlobalPositions::positions)).apply(instance, GlobalPositions::new));
	}

	public static StreamCodec<ByteBuf, GlobalPositions> streamCodec(int size) {
		return StreamCodec.composite(nullableSizedStreamCodec(GlobalPos.STREAM_CODEC, size, DUMMY_GLOBAL_POS), GlobalPositions::positions, GlobalPositions::new);
	}

	public static GlobalPositions sized(int size) {
		return new GlobalPositions(Arrays.asList(new GlobalPos[size]));
	}

	private static <A> Codec<List<A>> nullableSizedCodec(Codec<A> baseCodec, int size) {
		return new NullableListCodec<>(new Codec<>() {
			@Override
			public <R> DataResult<Pair<A, R>> decode(DynamicOps<R> ops, R input) {
				return input.equals(ops.emptyMap()) ? DataResult.success(Pair.of(null, input)) : baseCodec.decode(ops, input);
			}

			@Override
			public <R> DataResult<R> encode(A input, DynamicOps<R> ops, R prefix) {
				return input == null ? DataResult.success(ops.emptyMap()) : baseCodec.encode(input, ops, prefix);
			}
		}, size, size);
	}

	private static <A> StreamCodec<ByteBuf, List<A>> nullableSizedStreamCodec(StreamCodec<ByteBuf, A> baseStreamCodec, int size, A dummy) {
		return baseStreamCodec.map(value -> value.equals(dummy) ? null : value, value -> value == null ? dummy : value).apply(ByteBufCodecs.list(size));
	}

	public int size() {
		return positions.size();
	}

	public boolean isEmpty() {
		return positions.stream().allMatch(Objects::isNull);
	}

	public boolean isPositionAdded(GlobalPos globalPos) {
		return globalPos != null && positions.contains(globalPos);
	}

	/**
	 * Puts the given position into the first free slot and writes the result back onto the stack.
	 *
	 * @return true if the position was added, false if it was already bound or there was no free slot
	 */
	public boolean add(DataComponentType<GlobalPositions> componentType, ItemStack stack, GlobalPos globalPos) {
		if (!isPositionAdded(globalPos)) {
			List<GlobalPos> newPositions = new ArrayList<>(positions);

			for (int i = 0; i < newPositions.size(); i++) {
				if (newPositions.get(i) == null) {
					newPositions.set(i, globalPos);
					stack.set(componentType, new GlobalPositions(newPositions));
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Clears the slot holding the given position and writes the result back onto the stack.
	 *
	 * @return true if the position was bound and has been removed, false otherwise
	 */
	public boolean remove(DataComponentType<GlobalPositions> componentType, ItemStack stack, GlobalPos globalPos) {
		if (globalPos != null && !isEmpty()) {
			List<GlobalPos> newPositions = new ArrayList<>(positions);

			for (int i = 0; i < newPositions.size(); i++) {
				if (globalPos.equals(newPositions.get(i))) {
					newPositions.set(i, null);
					stack.set(componentType, new GlobalPositions(newPositions));
					return true;
				}
			}
		}

		return false;
	}
}
