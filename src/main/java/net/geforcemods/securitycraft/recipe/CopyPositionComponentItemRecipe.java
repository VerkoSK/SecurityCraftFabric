package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.GlobalPositions;
import net.geforcemods.securitycraft.items.MineRemoteAccessToolItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Combines a position-storing item that has data with an empty one of the same kind, copying the stored positions onto
 * both results. 1:1 with the upstream {@code recipe.CopyPositionComponentItemRecipe}, minus the generic plumbing for
 * the three other position-storing items this port does not have yet; the serializer is read from {@link SCContent} on
 * demand rather than passed in, because Fabric registers it eagerly and the constructor would otherwise need it before
 * it exists.
 */
public class CopyPositionComponentItemRecipe extends CombineRecipe {
	public CopyPositionComponentItemRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public static CopyPositionComponentItemRecipe mineRemoteAccessTool(CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe(craftingBookCategory);
	}

	@Override
	public boolean matchesFirstItem(ItemStack stack) {
		return stack.is(SCContent.MINE_REMOTE_ACCESS_TOOL) && !stack.getOrDefault(SCContent.BOUND_MINES, MineRemoteAccessToolItem.DEFAULT_POSITIONS).isEmpty();
	}

	@Override
	public boolean matchesSecondItem(ItemStack stack) {
		return stack.is(SCContent.MINE_REMOTE_ACCESS_TOOL) && stack.getOrDefault(SCContent.BOUND_MINES, MineRemoteAccessToolItem.DEFAULT_POSITIONS).isEmpty();
	}

	@Override
	public ItemStack combine(ItemStack itemWithPositions, ItemStack emptyItem) {
		GlobalPositions positionsToCopy = itemWithPositions.getOrDefault(SCContent.BOUND_MINES, MineRemoteAccessToolItem.DEFAULT_POSITIONS);
		ItemStack result = new ItemStack(SCContent.MINE_REMOTE_ACCESS_TOOL, 2);

		result.set(SCContent.BOUND_MINES, new GlobalPositions(positionsToCopy.positions()));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
