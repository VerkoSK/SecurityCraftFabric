package net.geforcemods.securitycraft.misc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.SCClientConfig;
import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Client-side check for a newer SecurityCraft release. Fetches {@code update_checker.json} from the repo once per game
 * session on a background thread; if the version listed for the running Minecraft version is newer than the installed
 * mod, {@link #notify} posts a one-off chat message with a link to the download page.
 */
public final class UpdateChecker {
	private static final String URL = "https://raw.githubusercontent.com/VerkoSK/SecurityCraftFabric/1.20.1/update_checker.json";
	private static String latestVersion;
	private static String homepage;
	private static boolean notified;

	private UpdateChecker() {}

	/** Kicks off the background fetch. Safe to call once from the client initializer. */
	public static void run() {
		if (!SCClientConfig.checkForUpdates)
			return;

		Thread thread = new Thread(UpdateChecker::fetch, "SecurityCraft Update Checker");

		thread.setDaemon(true);
		thread.start();
	}

	private static void fetch() {
		try {
			HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
			HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).timeout(Duration.ofSeconds(5)).header("Accept", "application/json").GET().build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200)
				return;

			JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
			JsonObject promos = json.getAsJsonObject("promos");
			String mcVersion = SharedConstants.getCurrentVersion().getName();

			if (promos != null && promos.has(mcVersion)) {
				latestVersion = promos.get(mcVersion).getAsString();
				homepage = json.has("homepage") ? json.get("homepage").getAsString() : "https://www.curseforge.com/minecraft/mc-mods/securitycraft";
			}
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.debug("SecurityCraft update check failed: {}", e.toString());
		}
	}

	/** Called on client join; shows the notification at most once per session. */
	public static void notify(java.util.function.Consumer<Component> messageSink) {
		if (notified || latestVersion == null)
			return;

		String current = FabricLoader.getInstance().getModContainer(SecurityCraft.MODID).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0");

		if (!isNewer(latestVersion, current))
			return;

		notified = true;

		MutableComponent link = Component.literal(homepage).withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, homepage)));

		messageSink.accept(Component.translatable("messages.securitycraft:updateChecker.updateAvailable", latestVersion, current).withStyle(ChatFormatting.GREEN));
		messageSink.accept(Component.translatable("messages.securitycraft:updateChecker.download", link).withStyle(ChatFormatting.GRAY));
	}

	/** Compares dotted numeric versions ("0.6" > "0.5"), ignoring any non-numeric suffix. */
	private static boolean isNewer(String candidate, String current) {
		int[] a = parse(candidate);
		int[] b = parse(current);

		for (int i = 0; i < Math.max(a.length, b.length); i++) {
			int x = i < a.length ? a[i] : 0;
			int y = i < b.length ? b[i] : 0;

			if (x != y)
				return x > y;
		}

		return false;
	}

	private static int[] parse(String version) {
		String[] parts = version.split("[.+-]");
		int[] out = new int[parts.length];

		for (int i = 0; i < parts.length; i++) {
			try {
				out[i] = Integer.parseInt(parts[i].trim());
			}
			catch (NumberFormatException e) {
				out[i] = 0;
			}
		}

		return out;
	}
}
