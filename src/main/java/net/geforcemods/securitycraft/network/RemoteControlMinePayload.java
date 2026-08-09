package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.api.IExplosive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Client -> server: the owner remotely activates, defuses or detonates a mine bound to their remote access tool. 1:1 with upstream RemoteControlMine. */
public record RemoteControlMinePayload(BlockPos pos, Action action) implements CustomPacketPayload {
	public static final Type<RemoteControlMinePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "remote_control_mine"));
	//@formatter:off
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoteControlMinePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, RemoteControlMinePayload::pos,
			enumStreamCodec(Action.class), RemoteControlMinePayload::action,
			RemoteControlMinePayload::new);
	//@formatter:on

	/** Stands in for NeoForge's {@code NeoForgeStreamCodecs.enumCodec}, which has no Fabric counterpart. */
	private static <E extends Enum<E>> StreamCodec<io.netty.buffer.ByteBuf, E> enumStreamCodec(Class<E> enumClass) {
		return net.minecraft.network.codec.ByteBufCodecs.idMapper(id -> enumClass.getEnumConstants()[id], Enum::ordinal);
	}

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

		/** Stands in for the Log4j {@code TriConsumer} upstream uses, so no logging class leaks into gameplay code. */
		@FunctionalInterface
		private interface MineAction {
			void accept(IExplosive explosive, Level level, BlockPos pos);
		}
	}
}
