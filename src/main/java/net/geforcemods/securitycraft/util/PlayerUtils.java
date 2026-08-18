package net.geforcemods.securitycraft.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerUtils {
	private PlayerUtils() {}

	/**
	 * Fabric adaptation of upstream's side-dispatching version: the call sites this port has are server-only, so the
	 * client branch (which upstream reaches through {@code EffectiveSide}) is dropped, and the return type is narrowed
	 * from upstream's {@code <T extends Player> T} to {@link ServerPlayer} accordingly.
	 *
	 * @param name The name of the player to look up
	 * @return The online player with the given name, or null if there is none
	 */
	public static ServerPlayer getPlayerFromName(String name) {
		MinecraftServer server = SecurityCraft.SERVER;

		if (server == null)
			return null;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getName().getString().equals(name))
				return player;
		}

		return null;
	}

	/**
	 * @param owner The owner whose player to look up
	 * @return A collection containing the owner's player if they are online, an empty collection otherwise
	 */
	public static Collection<ServerPlayer> getPlayerListFromOwner(Owner owner) {
		ServerPlayer player = getPlayerFromName(owner.getName());

		if (player != null)
			return Arrays.asList(player);

		return new ArrayList<>();
	}

	public static void sendMessageToPlayer(Player player, MutableComponent title, MutableComponent message, ChatFormatting color) {
		sendMessageToPlayer(player, title, message, color, false);
	}

	/**
	 * Sends a chat message from one side only, matching upstream. Callers like the mine remote access tool run on both
	 * the client and the server, so without this check the same message would be printed to chat twice.
	 */
	public static void sendMessageToPlayer(Player player, MutableComponent title, MutableComponent message, ChatFormatting color, boolean shouldSendFromClient) {
		if (player != null && player.level().isClientSide == shouldSendFromClient) {
			//only the name in the brackets carries the colour; the message itself stays white, like upstream.
			//withStyle on the whole component would tint the message too, which is what this used to do.
			MutableComponent line = Component.literal("[").append(title.setStyle(Style.EMPTY.withColor(color))).append(Component.literal("] ")).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).append(message);

			player.displayClientMessage(line, false);
		}
	}

	/**
	 * @param owner The owner to display
	 * @return The owner's team name in the team's colour if they own blocks as a team, their plain name otherwise
	 */
	public static Component getOwnerComponent(Owner owner) {
		TeamUtils.TeamRepresentation teamRepresentation = TeamUtils.getTeamRepresentation(owner);

		if (teamRepresentation != null)
			return Utils.localize("messages.securitycraft:teamOwner", Component.literal(teamRepresentation.name()).withStyle(Style.EMPTY.withColor(teamRepresentation.color()))).withStyle(ChatFormatting.GRAY);

		return Component.literal(owner.getName());
	}

	public static ItemStack getItemStackFromAnyHand(Player player, Item item) {
		if (player.getMainHandItem().is(item))
			return player.getMainHandItem();

		if (player.getOffhandItem().is(item))
			return player.getOffhandItem();

		return ItemStack.EMPTY;
	}
}
