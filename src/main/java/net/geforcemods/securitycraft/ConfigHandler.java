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
	/** Should players be able to break blocks owned by somebody else? (upstream default false) */
	public static boolean allowBreakingNonOwnedBlocks = false;
	/** How much slower the owner breaks their own reinforced blocks compared to the vanilla block. (upstream default 5.0) */
	public static double ownedBreakingSlowdown = 5.0;
	/** How much slower a non-owner breaks reinforced blocks, when breaking them is allowed at all. (upstream default 50.0) */
	public static double nonOwnedBreakingSlowdown = 50.0;
	/** Should players be able to claim blocks that have no owner yet, using the Universal Owner Changer? (upstream default true) */
	public static boolean allowBlockClaim = true;

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

				if (json.has("allow_breaking_non_owned_blocks"))
					allowBreakingNonOwnedBlocks = json.get("allow_breaking_non_owned_blocks").getAsBoolean();

				if (json.has("owned_breaking_slowdown"))
					ownedBreakingSlowdown = Math.max(1.0, json.get("owned_breaking_slowdown").getAsDouble());

				if (json.has("non_owned_breaking_slowdown"))
					nonOwnedBreakingSlowdown = Math.max(1.0, json.get("non_owned_breaking_slowdown").getAsDouble());

				if (json.has("allow_block_claim"))
					allowBlockClaim = json.get("allow_block_claim").getAsBoolean();

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
		json.addProperty("allow_breaking_non_owned_blocks", allowBreakingNonOwnedBlocks);
		json.addProperty("owned_breaking_slowdown", ownedBreakingSlowdown);
		json.addProperty("non_owned_breaking_slowdown", nonOwnedBreakingSlowdown);
		json.addProperty("allow_block_claim", allowBlockClaim);

		try {
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't write securitycraft-server.json", e);
		}
	}
}
