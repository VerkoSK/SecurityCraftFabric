package net.geforcemods.securitycraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Small shared helpers ported from the original SecurityCraft {@code util.Utils}. */
public class Utils {
	public static final Style GRAY_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);

	private Utils() {}

	public static MutableComponent localize(String key, Object... args) {
		return Component.translatable(key, args);
	}

	public static Component getFormattedCoordinates(BlockPos pos) {
		return Component.translatable("messages.securitycraft:formattedCoordinates", pos.getX(), pos.getY(), pos.getZ());
	}
}
