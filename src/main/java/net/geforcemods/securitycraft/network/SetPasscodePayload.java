package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Client -> server: the owner sets a new passcode on the keypad at {@code pos}. */
public record SetPasscodePayload(BlockPos pos, String passcode) implements CustomPayload {
	public static final CustomPayload.Id<SetPasscodePayload> ID = new CustomPayload.Id<>(Identifier.of("securitycraft", "set_passcode"));
	public static final PacketCodec<RegistryByteBuf, SetPasscodePayload> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, SetPasscodePayload::pos,
			PacketCodecs.STRING, SetPasscodePayload::passcode,
			SetPasscodePayload::new);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
