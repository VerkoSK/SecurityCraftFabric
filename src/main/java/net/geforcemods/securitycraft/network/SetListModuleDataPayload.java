package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.components.ListModuleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: update the allow/deny list stored on the held allowlist/denylist module. */
public record SetListModuleDataPayload(ListModuleData listModuleData) implements CustomPacketPayload {
	public static final Type<SetListModuleDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("securitycraft", "set_list_module_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetListModuleDataPayload> CODEC = StreamCodec.composite(
			ListModuleData.STREAM_CODEC, SetListModuleDataPayload::listModuleData,
			SetListModuleDataPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
