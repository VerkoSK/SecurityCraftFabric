package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.BoundMines;
import net.geforcemods.securitycraft.items.MineRemoteAccessToolItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Combines a bound mine remote access tool with an empty one, copying the bound mine positions onto both results.
 * Fabric adaptation of upstream's {@code CopyPositionComponentItemRecipe}: the upstream version compares raw NBT
 * tags, but this port stores the bound mines in the {@link BoundMines} data component instead, so the "has data"
 * check goes through {@link MineRemoteAccessToolItem#hasMineAdded} and the copy is a component merge instead of an
 * NBT merge.
 */
public class CopyPositionComponentItemRecipe extends CustomRecipe {
	@Override
	public boolean matches(CraftingInput inv, net.minecraft.world.level.Level level) {
		ItemStack withData = ItemStack.EMPTY;
		ItemStack withoutData = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);

			if (stack.isEmpty())
				continue;

			if (!(stack.getItem() instanceof Item item) || item != SCContent.MINE_REMOTE_ACCESS_TOOL)
				return false;

			if (MineRemoteAccessToolItem.hasMineAdded(stack)) {
				if (!withData.isEmpty())
					return false;

				withData = stack;
			}
			else {
				if (!withoutData.isEmpty())
					return false;

				withoutData = stack;
			}
		}

		return !withData.isEmpty() && !withoutData.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);

			if (!stack.isEmpty() && MineRemoteAccessToolItem.hasMineAdded(stack)) {
				ItemStack result = new ItemStack(SCContent.MINE_REMOTE_ACCESS_TOOL, 2);

				result.set(SCContent.BOUND_MINES, stack.get(SCContent.BOUND_MINES));
				return result;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
