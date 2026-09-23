package net.geforcemods.securitycraft.network;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server: the keycard reader/lock screen's accepted levels and signature (kept live so the block reflects
 * changes immediately), and, if {@code link} is set, links the keycard currently in the slot with those settings.
 */
public record SyncKeycardSettingsPayload(BlockPos pos, List<Boolean> acceptedLevels, int signature, boolean link, Optional<String> usableBy) implements CustomPacketPayload {
	public static final Type<SyncKeycardSettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "sync_keycard_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncKeycardSettingsPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncKeycardSettingsPayload::pos,
			ByteBufCodecs.BOOL.apply(ByteBufCodecs.list(5)), SyncKeycardSettingsPayload::acceptedLevels,
			ByteBufCodecs.VAR_INT, SyncKeycardSettingsPayload::signature,
			ByteBufCodecs.BOOL, SyncKeycardSettingsPayload::link,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), SyncKeycardSettingsPayload::usableBy,
			SyncKeycardSettingsPayload::new);

	public SyncKeycardSettingsPayload(BlockPos pos, boolean[] acceptedLevels, int signature, boolean link, Optional<String> usableBy) {
		this(pos, boxed(acceptedLevels), signature, link, usableBy);
	}

	private static List<Boolean> boxed(boolean[] values) {
		Boolean[] boxed = new Boolean[values.length];

		for (int i = 0; i < values.length; i++)
			boxed[i] = values[i];

		return List.of(boxed);
	}

	public boolean[] acceptedLevelsArray() {
		boolean[] values = new boolean[acceptedLevels.size()];

		for (int i = 0; i < values.length; i++)
			values[i] = acceptedLevels.get(i);

		return values;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
