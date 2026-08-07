package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.api.IExplosive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Client -> server: the owner remotely activates, defuses or detonates a mine bound to their remote access tool. 1:1 with upstream RemoteControlMine. */
public record RemoteControlMinePayload(BlockPos pos, Action action) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "remote_control_mine");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeEnum(action);
		return buf;
	}

	public static RemoteControlMinePayload read(FriendlyByteBuf buf) {
		return new RemoteControlMinePayload(buf.readBlockPos(), buf.readEnum(Action.class));
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
