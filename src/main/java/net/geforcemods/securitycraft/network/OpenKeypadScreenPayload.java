package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Server -> client: open the keypad screen at {@code pos} in setup or entry mode. */
public record OpenKeypadScreenPayload(BlockPos pos, boolean setup, String ownerName) implements CustomPayload {
	public static final CustomPayload.Id<OpenKeypadScreenPayload> ID = new CustomPayload.Id<>(Identifier.of("securitycraft", "open_keypad_screen"));
	public static final PacketCodec<RegistryByteBuf, OpenKeypadScreenPayload> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, OpenKeypadScreenPayload::pos,
			PacketCodecs.BOOLEAN, OpenKeypadScreenPayload::setup,
			PacketCodecs.STRING, OpenKeypadScreenPayload::ownerName,
			OpenKeypadScreenPayload::new);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
