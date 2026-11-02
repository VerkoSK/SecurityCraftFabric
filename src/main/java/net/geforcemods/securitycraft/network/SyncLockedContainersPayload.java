package net.geforcemods.securitycraft.network;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client: sync the list of positions locked by a Key Panel (ContainerLockData). */
public record SyncLockedContainersPayload(boolean clearExisting, List<BlockPos> locked, List<BlockPos> unlocked) implements CustomPacketPayload {
	public static final Type<SyncLockedContainersPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "sync_locked_containers"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncLockedContainersPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SyncLockedContainersPayload::clearExisting,
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncLockedContainersPayload::locked,
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncLockedContainersPayload::unlocked,
			SyncLockedContainersPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
