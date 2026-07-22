package net.geforcemods.securitycraft.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

/**
 * The allow/deny-list data stored on an allowlist/denylist module. On MC 1.20.1 there is no data-component
 * system, so this is serialised to plain NBT (under the {@code listModuleData} key) on the module stack.
 */
public record ListModuleData(List<String> players, List<String> teams, boolean affectEveryone) {
	public static final int MAX_PLAYERS = 50;
	public static final ListModuleData EMPTY = new ListModuleData(List.of(), List.of(), false);
	private static final String KEY = "listModuleData";

	public void writeToStack(ItemStack stack) {
		CompoundTag tag = new CompoundTag();
		ListTag playersTag = new ListTag();
		ListTag teamsTag = new ListTag();

		for (String player : players)
			playersTag.add(StringTag.valueOf(player));

		for (String team : teams)
			teamsTag.add(StringTag.valueOf(team));

		tag.put("players", playersTag);
		tag.put("teams", teamsTag);
		tag.putBoolean("affect_everyone", affectEveryone);
		stack.getOrCreateTag().put(KEY, tag);
	}

	public static ListModuleData fromStack(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains(KEY)) {
			CompoundTag tag = stack.getTag().getCompound(KEY);
			ListTag playersTag = tag.getList("players", Tag.TAG_STRING);
			ListTag teamsTag = tag.getList("teams", Tag.TAG_STRING);
			List<String> players = new ArrayList<>();
			List<String> teams = new ArrayList<>();

			for (int i = 0; i < playersTag.size(); i++)
				players.add(playersTag.getString(i));

			for (int i = 0; i < teamsTag.size(); i++)
				teams.add(teamsTag.getString(i));

			return new ListModuleData(players, teams, tag.getBoolean("affect_everyone"));
		}

		return EMPTY;
	}

	public ListModuleData addPlayer(ItemStack stack, String playerName) {
		if (players.size() == MAX_PLAYERS || isPlayerOnList(playerName))
			return this;

		List<String> newPlayers = new ArrayList<>(players);
		ListModuleData newListModuleData;

		newPlayers.add(playerName);
		newListModuleData = new ListModuleData(newPlayers, teams, affectEveryone);
		newListModuleData.writeToStack(stack);
		return newListModuleData;
	}

	public ListModuleData removePlayer(ItemStack stack, String playerName) {
		if (players.isEmpty() || !isPlayerOnList(playerName))
			return this;

		List<String> newPlayers = new ArrayList<>(players);
		ListModuleData newListModuleData;

		newPlayers.remove(playerName);
		newListModuleData = new ListModuleData(newPlayers, teams, affectEveryone);
		newListModuleData.writeToStack(stack);
		return newListModuleData;
	}

	public ListModuleData toggleTeam(ItemStack stack, String teamName) {
		List<String> newTeams = new ArrayList<>(teams);
		ListModuleData newListModuleData;

		if (isTeamOnList(teamName))
			newTeams.remove(teamName);
		else
			newTeams.add(teamName);

		newListModuleData = new ListModuleData(players, newTeams, affectEveryone);
		newListModuleData.writeToStack(stack);
		return newListModuleData;
	}

	public boolean isTeamOfPlayerOnList(Level level, String playerName) {
		PlayerTeam team = level.getScoreboard().getPlayersTeam(playerName);

		return team != null && isTeamOnList(team.getName());
	}

	public boolean isTeamOnList(String teamName) {
		return teams.contains(teamName);
	}

	public boolean isPlayerOnList(String playerName) {
		return players.stream().anyMatch(playerName::equalsIgnoreCase);
	}

	public void updateAffectEveryone(ItemStack stack, boolean newAffectEveryone) {
		if (newAffectEveryone != affectEveryone)
			new ListModuleData(players, teams, newAffectEveryone).writeToStack(stack);
	}

	public void addToTooltip(Consumer<Component> lineAdder) {
		if (affectEveryone)
			lineAdder.accept(Utils.localize("tooltip.securitycraft.component.list_module_data.affects_everyone").setStyle(Utils.GRAY_STYLE));
		else {
			lineAdder.accept(Utils.localize("tooltip.securitycraft.component.list_module_data.added_players", players.size()).setStyle(Utils.GRAY_STYLE));
			lineAdder.accept(Utils.localize("tooltip.securitycraft.component.list_module_data.added_teams", teams.size()).setStyle(Utils.GRAY_STYLE));
		}
	}
}
