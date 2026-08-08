package net.geforcemods.securitycraft;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Server-side gameplay config, a JSON adaptation of the original SecurityCraft {@code ConfigHandler.SERVER}.
 * Defaults and ranges are taken verbatim from the upstream mod.
 */
public final class ConfigHandler {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("securitycraft-server.json");
	/** At most from how many blocks away can a laser block connect to another laser block? (upstream default 5) */
	public static int laserBlockRange = 5;
	/** Damage inflicted to an entity passing through a laser with an installed harming module. (upstream default 10.0) */
	public static double laserDamage = 10.0;
	/** Damage a passcode-protected block deals to the player on an incorrect code, if a harming module is installed. (upstream default 4 = two hearts) */
	public static int incorrectPasscodeDamage = 4;
	/** Should mines spawn fire after exploding? (upstream default true) */
	public static boolean shouldSpawnFire = true;
	/** Should mines' explosions be smaller than usual? (upstream default false) */
	public static boolean smallerMineExplosion = false;
	/** Should mines explode if broken while in Creative mode? (upstream default true) */
	public static boolean mineExplodesWhenInCreative = true;
	/** Set this to false if you want mines to not break blocks when they explode. (upstream default true) */
	public static boolean mineExplosionsBreakBlocks = true;
	/** Set this to true to enable every player on a scoreboard team to own the blocks of every other player on the same team. (upstream default false) */
	public static boolean enableTeamOwnership = false;

	private ConfigHandler() {}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				JsonObject json = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();

				if (json.has("laserBlockRange"))
					laserBlockRange = Math.max(0, json.get("laserBlockRange").getAsInt());

				if (json.has("laser_damage"))
					laserDamage = Math.max(0.0, json.get("laser_damage").getAsDouble());

				if (json.has("incorrectPasscodeDamage"))
					incorrectPasscodeDamage = Math.max(1, json.get("incorrectPasscodeDamage").getAsInt());

				if (json.has("shouldSpawnFire"))
					shouldSpawnFire = json.get("shouldSpawnFire").getAsBoolean();

				if (json.has("smallerMineExplosion"))
					smallerMineExplosion = json.get("smallerMineExplosion").getAsBoolean();

				if (json.has("mineExplodesWhenInCreative"))
					mineExplodesWhenInCreative = json.get("mineExplodesWhenInCreative").getAsBoolean();

				if (json.has("mineExplosionsBreakBlocks"))
					mineExplosionsBreakBlocks = json.get("mineExplosionsBreakBlocks").getAsBoolean();

				if (json.has("enable_team_ownership"))
					enableTeamOwnership = json.get("enable_team_ownership").getAsBoolean();
			}
			else
				save();
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't read securitycraft-server.json, using defaults", e);
		}
	}

	public static void save() {
		JsonObject json = new JsonObject();

		json.addProperty("laserBlockRange", laserBlockRange);
		json.addProperty("laser_damage", laserDamage);
		json.addProperty("incorrectPasscodeDamage", incorrectPasscodeDamage);
		json.addProperty("shouldSpawnFire", shouldSpawnFire);
		json.addProperty("smallerMineExplosion", smallerMineExplosion);
		json.addProperty("mineExplodesWhenInCreative", mineExplodesWhenInCreative);
		json.addProperty("mineExplosionsBreakBlocks", mineExplosionsBreakBlocks);
		json.addProperty("enable_team_ownership", enableTeamOwnership);

		try {
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't write securitycraft-server.json", e);
		}
	}
}
