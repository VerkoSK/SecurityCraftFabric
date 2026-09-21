package net.geforcemods.securitycraft.components;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A keycard's link data: which reader network it answers to (signature), the one player name it's restricted to (empty
 * means everyone), who linked it, and whether it is limited-use (and if so, how many uses remain). On 1.20.1 this
 * lives directly in the stack's NBT under the "securitycraft:keycard" tag (plus separate "linked"/"limited" boolean
 * tags); this port has no NBT tags, so it is a proper data component instead (see {@code SCContent#KEYCARD_DATA}).
 * <p>
 * {@code linked} and {@code limited} are packed into one flags int (rather than two separate boolean fields) to stay
 * within {@link StreamCodec#composite}'s six-argument overload. No list field here, so unlike {@link GlobalPositions}
 * or {@link KeycardHolderData} there is no fixed-size padding to worry about for the validated-stream-codec trap.
 */
public record KeycardData(int signature, String usableBy, String ownerName, String ownerUUID, int usesLeft, int flags) {
	private static final int LINKED_FLAG = 1;
	private static final int LIMITED_FLAG = 2;

	public static final KeycardData EMPTY = new KeycardData(0, "", "", "", 0, 0);
	//@formatter:off
	public static final Codec<KeycardData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.fieldOf("signature").forGetter(KeycardData::signature),
					Codec.STRING.optionalFieldOf("usable_by", "").forGetter(KeycardData::usableBy),
					Codec.STRING.optionalFieldOf("owner_name", "").forGetter(KeycardData::ownerName),
					Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(KeycardData::ownerUUID),
					Codec.INT.optionalFieldOf("uses_left", 0).forGetter(KeycardData::usesLeft),
					Codec.INT.optionalFieldOf("flags", 0).forGetter(KeycardData::flags))
			.apply(instance, KeycardData::new));
	public static final StreamCodec<ByteBuf, KeycardData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, KeycardData::signature,
			ByteBufCodecs.STRING_UTF8, KeycardData::usableBy,
			ByteBufCodecs.STRING_UTF8, KeycardData::ownerName,
			ByteBufCodecs.STRING_UTF8, KeycardData::ownerUUID,
			ByteBufCodecs.VAR_INT, KeycardData::usesLeft,
			ByteBufCodecs.VAR_INT, KeycardData::flags,
			KeycardData::new);
	//@formatter:on

	public boolean linked() {
		return (flags & LINKED_FLAG) != 0;
	}

	public boolean limited() {
		return (flags & LIMITED_FLAG) != 0;
	}

	public Optional<String> usableByOptional() {
		return usableBy.isEmpty() ? Optional.empty() : Optional.of(usableBy);
	}

	/** Links this card to a reader, keeping its limited flag/uses as-is. */
	public KeycardData linkedWith(int newSignature, Optional<String> newUsableBy, String newOwnerName, String newOwnerUUID) {
		return new KeycardData(newSignature, newUsableBy.orElse(""), newOwnerName, newOwnerUUID, usesLeft, flags | LINKED_FLAG);
	}

	/** Makes this card limited-use, if it is not already (crafting with a Limited Use Keycard is a one-way change). */
	public KeycardData madeLimited() {
		return limited() ? this : new KeycardData(signature, usableBy, ownerName, ownerUUID, 0, flags | LIMITED_FLAG);
	}

	public KeycardData withUsesLeft(int newUsesLeft) {
		return limited() ? new KeycardData(signature, usableBy, ownerName, ownerUUID, Math.max(0, newUsesLeft), flags) : this;
	}
}
