package net.geforcemods.securitycraft.util;

import java.util.Collection;
import java.util.Objects;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

/**
 * 1:1 with the original SecurityCraft {@code util.TeamUtils}, collapsed onto its {@code VanillaTeamHandler}: the
 * upstream's TeamHandler precedence list exists solely to prefer FTB Teams parties over scoreboard teams, and this port
 * has no FTB Teams compat module. {@code ServerLifecycleHooks.getCurrentServer()} becomes {@link SecurityCraft#SERVER}.
 */
public class TeamUtils {
	/** Fallback colour for a team that has none set, matching the grey the other versions fall back to. */
	private static final int GRAY_RGB = 0xAAAAAA;

	private TeamUtils() {}

	public static boolean areOnSameTeam(Owner owner1, Owner owner2) {
		if (owner1.equals(owner2))
			return true;
		else if (!ConfigHandler.enableTeamOwnership)
			return false;

		PlayerTeam team = getVanillaTeamFromPlayer(owner1.getName());

		return team != null && team.getPlayers().contains(owner2.getName());
	}

	/**
	 * Gets the scoreboard team the given player is on
	 *
	 * @param playerName The player whose team to get
	 * @return The team the given player is on. null if the player is not part of a team
	 */
	public static PlayerTeam getVanillaTeamFromPlayer(String playerName) {
		MinecraftServer server = SecurityCraft.SERVER;

		return server == null ? null : server.getScoreboard().getPlayersTeam(playerName);
	}

	/**
	 * @param owner The owner whose team to represent
	 * @return The name and color of the given owner's team, or null if the owner is not part of a team with other members
	 */
	public static TeamRepresentation getTeamRepresentation(Owner owner) {
		if (ConfigHandler.enableTeamOwnership) {
			PlayerTeam team = getVanillaTeamFromPlayer(owner.getName());

			if (team != null && team.getPlayers().size() > 1) {
				//getColor() is an Optional<TeamColor> here, and the rgb value lives on TeamColor itself.
				return new TeamRepresentation(team.getDisplayName().getString(), team.getColor().map(net.minecraft.world.scores.TeamColor::rgb).orElse(GRAY_RGB));
			}
		}

		return null;
	}

	/**
	 * Gets all players that are in the same team as the given owner, and currently online
	 *
	 * @param server The server to look the players up on
	 * @param owner The owner whose team members to get
	 * @return The online team members, falling back to just the owner's own player if there is no team
	 */
	public static Collection<ServerPlayer> getOnlinePlayersFromOwner(MinecraftServer server, Owner owner) {
		if (ConfigHandler.enableTeamOwnership && server != null) {
			PlayerTeam team = getVanillaTeamFromPlayer(owner.getName());

			if (team != null) {
				Collection<ServerPlayer> onlinePlayers = team.getPlayers().stream().map(server.getPlayerList()::getPlayerByName).filter(Objects::nonNull).toList();

				if (!onlinePlayers.isEmpty())
					return onlinePlayers;
			}
		}

		return PlayerUtils.getPlayerListFromOwner(owner);
	}

	public record TeamRepresentation(String name, int color) {}
}
