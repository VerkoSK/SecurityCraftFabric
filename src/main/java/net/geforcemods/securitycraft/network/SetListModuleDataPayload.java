package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: update the allow/deny list stored on the held allowlist/denylist module. */
public record SetListModuleDataPayload(ListModuleData listModuleData) {
	public static final ResourceLocation CHANNEL = new ResourceLocation("securitycraft", "set_list_module_data");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeCollection(listModuleData.players(), FriendlyByteBuf::writeUtf);
		buf.writeCollection(listModuleData.teams(), FriendlyByteBuf::writeUtf);
		buf.writeBoolean(listModuleData.affectEveryone());
		return buf;
	}

	public static SetListModuleDataPayload read(FriendlyByteBuf buf) {
		return new SetListModuleDataPayload(new ListModuleData(buf.readList(FriendlyByteBuf::readUtf), buf.readList(FriendlyByteBuf::readUtf), buf.readBoolean()));
	}
}
