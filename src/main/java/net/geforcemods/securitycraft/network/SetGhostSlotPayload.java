package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.inventory.InventoryScannerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record SetGhostSlotPayload(int slotIndex, ItemStack stack) {
	public static final ResourceLocation CHANNEL = SecurityCraft.resLoc("set_ghost_slot");

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(slotIndex);
		buf.writeItem(stack);
		return buf;
	}

	public static SetGhostSlotPayload read(FriendlyByteBuf buf) {
		return new SetGhostSlotPayload(buf.readVarInt(), buf.readItem());
	}

	public void handle(ServerPlayer player) {
		if (!player.isSpectator() && player.containerMenu instanceof InventoryScannerMenu menu && menu.be.isOwnedBy(player))
			menu.be.getContents().set(slotIndex, stack);
	}
}
