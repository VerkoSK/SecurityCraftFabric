package net.geforcemods.securitycraft.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Small shared helpers ported from the original SecurityCraft {@code util.Utils}. */
public class Utils {
	private Utils() {}

	public static MutableComponent localize(String key, Object... args) {
		return Component.translatable(key, args);
	}
}
