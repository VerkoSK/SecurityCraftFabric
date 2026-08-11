package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.api.IExplosive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/** Client -> server: the owner remotely activates, defuses or detonates a mine bound to their remote access tool. 1:1 with upstream RemoteControlMine. */
public record RemoteControlMinePayload(BlockPos pos, Action action) implements CustomPacketPayload {
	public static final Type<RemoteControlMinePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "remote_control_mine"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoteControlMinePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, RemoteControlMinePayload::pos,
			ByteBufCodecs.idMapper(i -> Action.values()[i], Action::ordinal), RemoteControlMinePayload::action,
			RemoteControlMinePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public enum Action {
		ACTIVATE(IExplosive::activateMine),
		DEFUSE(IExplosive::defuseMine),
		DETONATE(IExplosive::explode);

		private final MineAction action;

		Action(MineAction action) {
			this.action = action;
		}

		public void act(IExplosive explosive, Level level, BlockPos pos) {
			action.accept(explosive, level, pos);
		}

		@FunctionalInterface
		private interface MineAction {
			void accept(IExplosive explosive, Level level, BlockPos pos);
		}
	}
}
