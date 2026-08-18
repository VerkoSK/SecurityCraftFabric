package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: enable or disable a module that is already inserted in a block, without taking it out.
 * 1:1 with upstream's ToggleModule packet, minus its entity branch (no module-carrying entity is ported yet).
 */
public record ToggleModulePayload(BlockPos pos, ModuleType moduleType) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "toggle_module");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(pos);
		buf.writeEnum(moduleType);
		return buf;
	}

	public static ToggleModulePayload read(FriendlyByteBuf buf) {
		return new ToggleModulePayload(buf.readBlockPos(), buf.readEnum(ModuleType.class));
	}
}
