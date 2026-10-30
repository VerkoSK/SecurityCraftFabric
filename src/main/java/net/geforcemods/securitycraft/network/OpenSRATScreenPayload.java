package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record OpenSRATScreenPayload(CompoundTag tag) {
	public static final ResourceLocation CHANNEL = SCContent.id("open_srat_screen");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeNbt(tag);
		return buf;
	}

	public static OpenSRATScreenPayload read(FriendlyByteBuf buf) {
		return new OpenSRATScreenPayload(buf.readNbt());
	}
}
