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

	private ConfigHandler() {}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				JsonObject json = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();

				if (json.has("laserBlockRange"))
					laserBlockRange = Math.max(0, json.get("laserBlockRange").getAsInt());

				if (json.has("laser_damage"))
					laserDamage = Math.max(0.0, json.get("laser_damage").getAsDouble());
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

		try {
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't write securitycraft-server.json", e);
		}
	}
}
