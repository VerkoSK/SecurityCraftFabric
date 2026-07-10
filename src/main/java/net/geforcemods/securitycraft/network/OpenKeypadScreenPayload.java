package net.geforcemods.securitycraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: open the keypad screen at {@code pos} in setup or entry mode. */
public record OpenKeypadScreenPayload(BlockPos pos, boolean setup, String ownerName) implements CustomPacketPayload {
	public static final Type<OpenKeypadScreenPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "open_keypad_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenKeypadScreenPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenKeypadScreenPayload::pos,
			ByteBufCodecs.BOOL, OpenKeypadScreenPayload::setup,
			ByteBufCodecs.STRING_UTF8, OpenKeypadScreenPayload::ownerName,
			OpenKeypadScreenPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
