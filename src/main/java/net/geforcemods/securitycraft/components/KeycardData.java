package net.geforcemods.securitycraft.components;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A keycard's link data: which reader network it answers to (signature), the one player name it's restricted to (empty
 * means everyone), who linked it, and (for a limited-use card) how many uses remain. On 1.20.1 this lives directly in
 * the stack's NBT under the "securitycraft:keycard" tag; this port has no NBT tags, so it is a proper data component
 * instead (see {@code SCContent#KEYCARD_DATA}). No list field here, so unlike {@link GlobalPositions} or
 * {@link KeycardHolderData} there is no fixed-size padding to worry about for the validated-stream-codec trap.
 */
public record KeycardData(int signature, String usableBy, String ownerName, String ownerUUID, int usesLeft) {
	public static final KeycardData EMPTY = new KeycardData(0, "", "", "", 0);
	//@formatter:off
	public static final Codec<KeycardData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.fieldOf("signature").forGetter(KeycardData::signature),
					Codec.STRING.optionalFieldOf("usable_by", "").forGetter(KeycardData::usableBy),
					Codec.STRING.optionalFieldOf("owner_name", "").forGetter(KeycardData::ownerName),
					Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(KeycardData::ownerUUID),
					Codec.INT.optionalFieldOf("uses_left", 0).forGetter(KeycardData::usesLeft))
			.apply(instance, KeycardData::new));
	public static final StreamCodec<ByteBuf, KeycardData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, KeycardData::signature,
			ByteBufCodecs.STRING_UTF8, KeycardData::usableBy,
			ByteBufCodecs.STRING_UTF8, KeycardData::ownerName,
			ByteBufCodecs.STRING_UTF8, KeycardData::ownerUUID,
			ByteBufCodecs.VAR_INT, KeycardData::usesLeft,
			KeycardData::new);
	//@formatter:on

	public Optional<String> usableByOptional() {
		return usableBy.isEmpty() ? Optional.empty() : Optional.of(usableBy);
	}
}
