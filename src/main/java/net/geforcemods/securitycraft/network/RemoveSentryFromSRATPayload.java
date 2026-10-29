package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.network.FriendlyByteBuf;

public record RemoveSentryFromSRATPayload(int sentryIndex) implements SCPayload {
	public static final PacketType<RemoveSentryFromSRATPayload> TYPE = PacketType.create(SCContent.id("remove_sentry_from_srat"), RemoveSentryFromSRATPayload::new);

	public RemoveSentryFromSRATPayload(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(sentryIndex);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
