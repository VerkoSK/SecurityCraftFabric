package net.geforcemods.securitycraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Server-side gameplay config, a JSON adaptation of the original SecurityCraft {@code ConfigHandler.SERVER}.
 * Defaults and ranges are taken verbatim from the upstream mod.
 */
public final class ConfigHandler {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("securitycraft-server.json");
	public static final Server SERVER = new Server();

	public static final class Server {
		public final IntValue inventoryScannerRange = new IntValue(() -> ConfigHandler.inventoryScannerRange);
		public final DoubleValue taserDamage = new DoubleValue(() -> ConfigHandler.taserDamage);
		public final DoubleValue poweredTaserDamage = new DoubleValue(() -> ConfigHandler.poweredTaserDamage);
		public final DoubleValue sentryBulletDamage = new DoubleValue(() -> ConfigHandler.sentryBulletDamage);
		public final BooleanValue allowBlockClaim = new BooleanValue(() -> ConfigHandler.allowBlockClaim);
		public final BooleanValue trickScannersWithPlayerHeads = new BooleanValue(() -> ConfigHandler.trickScannersWithPlayerHeads);
		public final BooleanValue respectInvisibility = new BooleanValue(() -> ConfigHandler.respectInvisibility);
		public final BooleanValue vanillaToolBlockBreaking = new BooleanValue(() -> ConfigHandler.vanillaToolBlockBreaking);
		public final ConfigListValue sentryAttackableEntitiesAllowlist = new ConfigListValue(() -> ConfigHandler.sentryAttackableEntitiesAllowlist);
		public final ConfigListValue sentryAttackableEntitiesDenylist = new ConfigListValue(() -> ConfigHandler.sentryAttackableEntitiesDenylist);
		public final ConfigListValue taserEffects = new ConfigListValue(() -> ConfigHandler.taserEffects);
		public final ConfigListValue poweredTaserEffects = new ConfigListValue(() -> ConfigHandler.poweredTaserEffects);
	}

	public record IntValue(java.util.function.IntSupplier getter) {
		public int get() {
			return getter.getAsInt();
		}
	}

	public record DoubleValue(java.util.function.DoubleSupplier getter) {
		public double get() {
			return getter.getAsDouble();
		}
	}

	public record BooleanValue(java.util.function.BooleanSupplier getter) {
		public boolean get() {
			return getter.getAsBoolean();
		}
	}

	public record ConfigListValue(java.util.function.Supplier<List<String>> getter) {
		public List<? extends String> get() {
			return getter.get();
		}
	}

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
	/** 1:1 with upstream's always_drop: SecurityCraft's blocks drop themselves no matter which tool was used. */
	public static boolean alwaysDrop = true;
	public static boolean allowBreakingNonOwnedBlocks = false;
	/** How much slower the owner breaks their own reinforced blocks compared to the vanilla block. (upstream default 1.0) */
	public static double ownedBreakingSlowdown = 1.0;
	/** How much slower a non-owner breaks reinforced blocks, when breaking them is allowed at all. (upstream default 1.0) */
	public static double nonOwnedBreakingSlowdown = 1.0;
	/** Should players be able to claim blocks that have no owner yet, using the Universal Owner Changer? (upstream default true) */
	public static boolean allowBlockClaim = true;
	/** Chance (0.0-1.0) that a single Codebreaker use breaks into a passcode-protected block. (upstream default 0.33) */
	public static double codebreakerChance = 0.33;
	/** Should the Codebreaker item be usable at all? (upstream default true) */
	public static boolean allowCodebreakerItem = true;
	/** Volume multiplier for the Alarm block sound. (upstream default 1.0, range 0.0-5.0) */
	public static double alarmSoundVolume = 1.0;
	/** Maximum range (in blocks) of the Alarm block sound. (upstream default 100, range 1-1000) */
	public static int maxAlarmRange = 100;
	/** Should players be able to activate night vision while viewing a Security Camera? (upstream default true) */
	public static boolean allowCameraNightVision = true;
	/** Should the Retinal Scanner display the owner's face on its front? (upstream default true) */
	public static boolean retinalScannerFace = true;
	public static boolean trickScannersWithPlayerHeads = false;
	public static boolean respectInvisibility = false;
	public static boolean vanillaToolBlockBreaking = true;
	public static int reinforcedSuffocationDamage = 10;

	// --- V0.8 (Sentry, Taser, Inventory Scanner) ---
	public static int inventoryScannerRange = 2;
	public static double taserDamage = 1.0D;
	public static double poweredTaserDamage = 2.0D;
	public static double sentryBulletDamage = 2.0D;
	public static List<String> sentryAttackableEntitiesAllowlist = List.of();
	public static List<String> sentryAttackableEntitiesDenylist = List.of();
	public static List<String> taserEffects = List.of("minecraft:weakness|1|200", "minecraft:nausea|1|200", "minecraft:slowness|1|200");
	public static List<String> poweredTaserEffects = List.of("minecraft:weakness|3|400", "minecraft:nausea|3|400", "minecraft:slowness|3|400");

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

				if (json.has("always_drop"))
					alwaysDrop = json.get("always_drop").getAsBoolean();

				if (json.has("owned_breaking_slowdown"))
					ownedBreakingSlowdown = Math.max(0.0, json.get("owned_breaking_slowdown").getAsDouble());

				if (json.has("non_owned_breaking_slowdown"))
					nonOwnedBreakingSlowdown = Math.max(0.0, json.get("non_owned_breaking_slowdown").getAsDouble());

				if (json.has("allow_block_claim"))
					allowBlockClaim = json.get("allow_block_claim").getAsBoolean();

				if (json.has("codebreaker_chance"))
					codebreakerChance = Math.max(0.0, Math.min(1.0, json.get("codebreaker_chance").getAsDouble()));

				if (json.has("allow_codebreaker_item"))
					allowCodebreakerItem = json.get("allow_codebreaker_item").getAsBoolean();

				if (json.has("alarm_sound_volume"))
					alarmSoundVolume = Math.max(0.0, Math.min(5.0, json.get("alarm_sound_volume").getAsDouble()));

				if (json.has("max_alarm_range"))
					maxAlarmRange = Math.max(1, Math.min(1000, json.get("max_alarm_range").getAsInt()));

				if (json.has("allow_camera_night_vision"))
					allowCameraNightVision = json.get("allow_camera_night_vision").getAsBoolean();

				if (json.has("retinal_scanner_face"))
					retinalScannerFace = json.get("retinal_scanner_face").getAsBoolean();

				if (json.has("inventory_scanner_range"))
					inventoryScannerRange = Math.max(0, json.get("inventory_scanner_range").getAsInt());

				if (json.has("taser_damage"))
					taserDamage = Math.max(0.0, json.get("taser_damage").getAsDouble());

				if (json.has("powered_taser_damage"))
					poweredTaserDamage = Math.max(0.0, json.get("powered_taser_damage").getAsDouble());

				if (json.has("sentry_bullet_damage"))
					sentryBulletDamage = Math.max(0.0, json.get("sentry_bullet_damage").getAsDouble());

				if (json.has("sentry_attackable_entities_allowlist") && json.get("sentry_attackable_entities_allowlist").isJsonArray())
					sentryAttackableEntitiesAllowlist = readStringList(json.getAsJsonArray("sentry_attackable_entities_allowlist"));

				if (json.has("sentry_attackable_entities_denylist") && json.get("sentry_attackable_entities_denylist").isJsonArray())
					sentryAttackableEntitiesDenylist = readStringList(json.getAsJsonArray("sentry_attackable_entities_denylist"));

				if (json.has("taser_effects") && json.get("taser_effects").isJsonArray())
					taserEffects = readStringList(json.getAsJsonArray("taser_effects"));

				if (json.has("powered_taser_effects") && json.get("powered_taser_effects").isJsonArray())
					poweredTaserEffects = readStringList(json.getAsJsonArray("powered_taser_effects"));
			}
			else
				save();
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't read securitycraft-server.json, using defaults", e);
		}
	}

	private static List<String> readStringList(JsonArray array) {
		java.util.ArrayList<String> list = new java.util.ArrayList<>(array.size());

		for (JsonElement el : array) {
			list.add(el.getAsString());
		}

		return List.copyOf(list);
	}

	private static JsonArray toJsonArray(List<String> list) {
		JsonArray array = new JsonArray();

		for (String s : list) {
			array.add(s);
		}

		return array;
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
		json.addProperty("always_drop", alwaysDrop);
		json.addProperty("owned_breaking_slowdown", ownedBreakingSlowdown);
		json.addProperty("non_owned_breaking_slowdown", nonOwnedBreakingSlowdown);
		json.addProperty("allow_block_claim", allowBlockClaim);
		json.addProperty("codebreaker_chance", codebreakerChance);
		json.addProperty("allow_codebreaker_item", allowCodebreakerItem);
		json.addProperty("alarm_sound_volume", alarmSoundVolume);
		json.addProperty("max_alarm_range", maxAlarmRange);
		json.addProperty("allow_camera_night_vision", allowCameraNightVision);
		json.addProperty("retinal_scanner_face", retinalScannerFace);
		json.addProperty("inventory_scanner_range", inventoryScannerRange);
		json.addProperty("taser_damage", taserDamage);
		json.addProperty("powered_taser_damage", poweredTaserDamage);
		json.addProperty("sentry_bullet_damage", sentryBulletDamage);
		json.add("sentry_attackable_entities_allowlist", toJsonArray(sentryAttackableEntitiesAllowlist));
		json.add("sentry_attackable_entities_denylist", toJsonArray(sentryAttackableEntitiesDenylist));
		json.add("taser_effects", toJsonArray(taserEffects));
		json.add("powered_taser_effects", toJsonArray(poweredTaserEffects));

		try {
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't write securitycraft-server.json", e);
		}
	}
}
