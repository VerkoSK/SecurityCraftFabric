package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: enable or disable a module that is already inserted in a block, without taking it out.
 * 1:1 with upstream's ToggleModule packet, minus its entity branch (no module-carrying entity is ported yet).
 */
public record ToggleModulePayload(BlockPos pos, ModuleType moduleType) implements CustomPacketPayload {
	public static final Type<ToggleModulePayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "toggle_module"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleModulePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ToggleModulePayload::pos,
			//upstream uses NeoForgeStreamCodecs#enumCodec; sending the ordinal is the same thing
			ByteBufCodecs.VAR_INT.map(id -> ModuleType.values()[id], ModuleType::ordinal), ToggleModulePayload::moduleType,
			ToggleModulePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
