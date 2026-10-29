package net.geforcemods.securitycraft.network;

import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record SetSentryModePayload(List<Info> sentries) implements SCPayload {
	public static final PacketType<SetSentryModePayload> TYPE = PacketType.create(SCContent.id("set_sentry_mode"), SetSentryModePayload::new);

	public SetSentryModePayload(FriendlyByteBuf buf) {
		this(buf.readList(Info::read));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeCollection(sentries, Info::write);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public record Info(BlockPos pos, int mode) {
		public static Info read(FriendlyByteBuf buf) {
			return new Info(buf.readBlockPos(), buf.readVarInt());
		}

		public static void write(FriendlyByteBuf buf, Info info) {
			buf.writeBlockPos(info.pos);
			buf.writeVarInt(info.mode);
		}
	}
}
