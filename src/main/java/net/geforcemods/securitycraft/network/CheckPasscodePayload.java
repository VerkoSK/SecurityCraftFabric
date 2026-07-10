package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Client -> server: attempt a passcode on the keypad at {@code pos}. */
public record CheckPasscodePayload(BlockPos pos, String passcode) implements CustomPayload {
	public static final CustomPayload.Id<CheckPasscodePayload> ID = new CustomPayload.Id<>(Identifier.of("securitycraft", "check_passcode"));
	public static final PacketCodec<RegistryByteBuf, CheckPasscodePayload> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, CheckPasscodePayload::pos,
			PacketCodecs.STRING, CheckPasscodePayload::passcode,
			CheckPasscodePayload::new);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
