package net.geforcemods.securitycraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Small shared helpers ported from the original SecurityCraft {@code util.Utils}. */
public class Utils {
	public static final Style GRAY_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
	/** NBT key holding a lens's dyed colour (0xRRGGBB) — replaces the 1.20.5+ {@code dyed_color} data component on MC 1.20.1. */
	public static final String LENS_COLOR_KEY = "dyed_color";

	private Utils() {}

	public static MutableComponent localize(String key, Object... args) {
		return Component.translatable(key, args);
	}

	public static Component getFormattedCoordinates(BlockPos pos) {
		return Component.translatable("messages.securitycraft:formattedCoordinates", pos.getX(), pos.getY(), pos.getZ());
	}

	public static ItemStack parseOptional(CompoundTag tag) {
		return tag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(tag);
	}

	public static boolean hasLensColor(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains(LENS_COLOR_KEY);
	}

	public static int getLensColor(ItemStack stack) {
		return stack.getTag().getInt(LENS_COLOR_KEY);
	}

	public static void setLensColor(ItemStack stack, int rgb) {
		stack.getOrCreateTag().putInt(LENS_COLOR_KEY, rgb & 0xFFFFFF);
	}

	/** The namespace an {@link net.geforcemods.securitycraft.api.Option}'s translation key is filed under. 1:1 with the upstream method of the same name. */
	public static String getLanguageKeyDenotation(Object obj) {
		if (obj instanceof BlockEntity be)
			return getLanguageKeyDenotation(be.getBlockState().getBlock());
		else if (obj instanceof Block block)
			return block.getDescriptionId().substring(6);
		else if (obj instanceof Entity entity)
			return entity.getType().toShortString();
		else if (obj instanceof BlockState state)
			return getLanguageKeyDenotation(state.getBlock());
		else
			return "";
	}
}
