package net.geforcemods.securitycraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerUtils {
	private PlayerUtils() {}

	public static void sendMessageToPlayer(Player player, MutableComponent title, MutableComponent message, ChatFormatting color) {
		player.sendSystemMessage(Component.literal("[").append(title).append("] ").append(message).withStyle(color));
	}

	public static ItemStack getItemStackFromAnyHand(Player player, Item item) {
		if (player.getMainHandItem().is(item))
			return player.getMainHandItem();

		if (player.getOffhandItem().is(item))
			return player.getOffhandItem();

		return ItemStack.EMPTY;
	}
}
