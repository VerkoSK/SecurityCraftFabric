package net.geforcemods.securitycraft.recipe;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Combines a lens with one or more dyes to tint it, mixing colours like vanilla leather-armour dyeing. On MC
 * 1.20.1 vanilla JSON recipes cannot set arbitrary item data, so this custom recipe replaces the 16
 * result-component coloured-lens recipes from newer versions, writing the colour to the lens's NBT instead.
 */
public class LensColoringRecipe extends CustomRecipe {
	public LensColoringRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		ItemStack lens = ItemStack.EMPTY;
		boolean hasDye = false;

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);

			if (stack.isEmpty())
				continue;

			if (stack.is(SCContent.LENS)) {
				if (!lens.isEmpty())
					return false;

				lens = stack;
			}
			else if (stack.getItem() instanceof DyeItem)
				hasDye = true;
			else
				return false;
		}

		return !lens.isEmpty() && hasDye;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack lens = ItemStack.EMPTY;
		List<DyeItem> dyes = new ArrayList<>();

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);

			if (stack.isEmpty())
				continue;

			if (stack.is(SCContent.LENS)) {
				if (!lens.isEmpty())
					return ItemStack.EMPTY;

				lens = stack;
			}
			else if (stack.getItem() instanceof DyeItem dye)
				dyes.add(dye);
			else
				return ItemStack.EMPTY;
		}

		if (lens.isEmpty() || dyes.isEmpty())
			return ItemStack.EMPTY;

		ItemStack result = lens.copy();
		int[] colors = new int[3];
		int maxSum = 0;
		int count = 0;

		result.setCount(1);

		if (Utils.hasLensColor(lens)) {
			int existing = Utils.getLensColor(lens);
			int r = existing >> 16 & 255;
			int g = existing >> 8 & 255;
			int b = existing & 255;

			maxSum += Math.max(r, Math.max(g, b));
			colors[0] += r;
			colors[1] += g;
			colors[2] += b;
			count++;
		}

		for (DyeItem dye : dyes) {
			float[] diffuse = dye.getDyeColor().getTextureDiffuseColors();
			int r = (int) (diffuse[0] * 255.0F);
			int g = (int) (diffuse[1] * 255.0F);
			int b = (int) (diffuse[2] * 255.0F);

			maxSum += Math.max(r, Math.max(g, b));
			colors[0] += r;
			colors[1] += g;
			colors[2] += b;
			count++;
		}

		int red = colors[0] / count;
		int green = colors[1] / count;
		int blue = colors[2] / count;
		float averageMax = (float) maxSum / count;
		float actualMax = Math.max(red, Math.max(green, blue));

		if (actualMax > 0.0F) {
			float scale = averageMax / actualMax;

			red = (int) (red * scale);
			green = (int) (green * scale);
			blue = (int) (blue * scale);
		}

		Utils.setLensColor(result, red << 16 | green << 8 | blue);
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SCContent.LENS_COLORING_SERIALIZER;
	}
}
