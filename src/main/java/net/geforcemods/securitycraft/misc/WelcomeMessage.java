package net.geforcemods.securitycraft.misc;

import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Client-side "thanks for using SecurityCraft" chat message shown once per session on world join, matching
 * upstream's {@code Tips} class. Trimmed to what applies to this port (no Patreon, no official server, no
 * {@code /sc help} command to point at), and with a bug-report line pointing at this port's own Discord channel
 * instead of upstream's - not part of upstream.
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

		String version = FabricLoader.getInstance().getModContainer(SecurityCraft.MODID).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0");
		MutableComponent prefix = Component.literal("[").append(Component.literal("SecurityCraft").withStyle(ChatFormatting.GOLD)).append(Component.literal("] "));
		MutableComponent discordLink = Component.literal("Discord").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DISCORD_URL)));

		messageSink.accept(prefix.copy().append(Utils.localize("messages.securitycraft:thanks", version)));
		messageSink.accept(prefix.copy().append(Utils.localize("messages.securitycraft:bugReport", discordLink)));
	}
}
