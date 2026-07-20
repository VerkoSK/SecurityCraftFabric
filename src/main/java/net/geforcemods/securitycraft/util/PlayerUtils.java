package net.geforcemods.securitycraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class PlayerUtils {
	private PlayerUtils() {}

	public static void sendMessageToPlayer(Player player, MutableComponent title, MutableComponent message, ChatFormatting color) {
		player.displayClientMessage(Component.literal("[").append(title).append("] ").append(message).withStyle(color), false);
	}
}
