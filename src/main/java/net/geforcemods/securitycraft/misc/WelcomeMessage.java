package net.geforcemods.securitycraft.misc;

import java.net.URI;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Client-side "thanks for using SecurityCraft" chat message shown once per session on world join, matching
 * upstream's {@code Tips} class. Includes Fabric port authorship and official repository links.
 */
public final class WelcomeMessage {
	private static final String DISCORD_URL = "https://discord.com/channels/798455990341206056/1222259012310007828";
	private static boolean shown;

	private WelcomeMessage() {}

	/** Called on client join; shows the message at most once per session. */
	public static void show(Consumer<Component> messageSink) {
		if (shown)
			return;

		shown = true;

		String version = FabricLoader.getInstance().getModContainer(SecurityCraft.MODID).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0.7");
		MutableComponent prefix = Component.literal("[").append(Component.literal("SecurityCraft").withStyle(ChatFormatting.GOLD)).append(Component.literal("] "));
		MutableComponent discordLink = Component.literal("Discord").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent.OpenUrl(URI.create(DISCORD_URL))));
		MutableComponent githubLink = Component.literal("GitHub").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent.OpenUrl(URI.create(SecurityCraft.PORT_REPO))));

		messageSink.accept(prefix.copy().append(Component.literal("Fabric port by ").withStyle(ChatFormatting.YELLOW)).append(Component.literal(SecurityCraft.PORT_AUTHOR).withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD)).append(Component.literal(" v" + version).withStyle(ChatFormatting.GRAY)));
		messageSink.accept(prefix.copy().append(Component.literal("Source & Issues: ").withStyle(ChatFormatting.GRAY)).append(githubLink).append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(discordLink));
	}
}
