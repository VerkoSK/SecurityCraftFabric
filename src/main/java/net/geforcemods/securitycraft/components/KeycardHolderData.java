package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * A fixed-size list of keycards stored on a Keycard Holder, where an empty slot is {@link ItemStack#EMPTY}. Same shape
 * as {@code GlobalPositions}: both the persistent codec and the network stream codec pad or truncate to exactly
 * {@code size} entries on decode, since an exact-size stream codec otherwise rejects any list whose encoded length
 * doesn't match during {@code ItemStack.validatedStreamCodec} re-encoding (e.g. on the set_creative_mode_slot packet),
 * dropping the connection - a trap already hit once this session on another component.
 */
public record KeycardHolderData(List<ItemStack> cards) {
	public static KeycardHolderData sized(int size) {
		List<ItemStack> list = new ArrayList<>(size);

		for (int i = 0; i < size; i++)
			list.add(ItemStack.EMPTY);

		return new KeycardHolderData(list);
	}

	public static Codec<KeycardHolderData> codec(int size) {
		//@formatter:off
		return RecordCodecBuilder.create(
				instance -> instance.group(ItemStack.OPTIONAL_CODEC.listOf().fieldOf("cards").forGetter(data -> resized(data.cards(), size)))
				.apply(instance, cards -> new KeycardHolderData(resized(cards, size))));
		//@formatter:on
	}

	public static StreamCodec<RegistryFriendlyByteBuf, KeycardHolderData> streamCodec(int size) {
		//@formatter:off
		return StreamCodec.composite(
				ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(size)), data -> resized(data.cards(), size),
				cards -> new KeycardHolderData(resized(cards, size)));
		//@formatter:on
	}

	/** Forces a decoded list to exactly {@code size} slots, padding with {@link ItemStack#EMPTY} or dropping overflow. */
	private static List<ItemStack> resized(List<ItemStack> cards, int size) {
		if (cards.size() == size)
			return cards;

		List<ItemStack> resized = new ArrayList<>(size);

		for (int i = 0; i < size; i++)
			resized.add(i < cards.size() ? cards.get(i) : ItemStack.EMPTY);

		return resized;
	}
}
