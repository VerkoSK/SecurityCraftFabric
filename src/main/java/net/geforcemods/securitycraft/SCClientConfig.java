package net.geforcemods.securitycraft;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.misc.TintMode;

/** Persistent client-side settings: the reinforced-block tint mode and colour. */
public final class SCClientConfig {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("securitycraft-client.json");
	public static int tintColor = 0x999999;
	public static TintMode tintMode = TintMode.ALL;
	/** Whether to check for a newer SecurityCraft release on join and show a chat notification. */
	public static boolean checkForUpdates = true;

	private SCClientConfig() {}

	/** Loads the config (if present) and pushes the values into the live {@link TintMode} state. */
	public static void load() {
		try {
			if (Files.exists(FILE)) {
				JsonObject json = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();

				if (json.has("reinforced_block_tint_color"))
					tintColor = json.get("reinforced_block_tint_color").getAsInt();

				if (json.has("reinforced_block_tint_mode"))
					tintMode = TintMode.valueOf(json.get("reinforced_block_tint_mode").getAsString());

				if (json.has("check_for_updates"))
					checkForUpdates = json.get("check_for_updates").getAsBoolean();
			}
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't read securitycraft-client.json, using defaults", e);
		}

		TintMode.setColor(tintColor);
		TintMode.setMode(tintMode);
	}

	/** Copies the live {@link TintMode} state into this config and writes it to disk. */
	public static void save() {
		tintColor = TintMode.color();
		tintMode = TintMode.mode();

		JsonObject json = new JsonObject();

		json.addProperty("reinforced_block_tint_color", tintColor);
		json.addProperty("reinforced_block_tint_mode", tintMode.name());
		json.addProperty("check_for_updates", checkForUpdates);

		try {
			Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Couldn't write securitycraft-client.json", e);
		}
	}
}
