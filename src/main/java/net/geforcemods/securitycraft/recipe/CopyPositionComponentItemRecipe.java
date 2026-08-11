package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.BoundMines;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Combines a position-storing item that has data with an empty one of the same kind, copying the stored positions
 * onto both results. Fabric adaptation of the upstream generic {@code CopyPositionComponentItemRecipe}: this port
 * only has the mine remote access tool's bound-mine slots, stored in {@link BoundMines} rather than upstream's
 * generic {@code GlobalPositions}, so the recipe is specialised to that single item instead of templated.
 */
public class CopyPositionComponentItemRecipe extends CombineRecipe {
	private final Item item;

	public CopyPositionComponentItemRecipe(CraftingBookCategory craftingBookCategory, Item item) {
		super(craftingBookCategory);
		this.item = item;
	}

	public static CopyPositionComponentItemRecipe mineRemoteAccessTool(CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe(craftingBookCategory, SCContent.MINE_REMOTE_ACCESS_TOOL);
	}

	@Override
	public boolean matchesFirstItem(ItemStack stack) {
		return stack.is(item) && !stack.getOrDefault(SCContent.BOUND_MINES, BoundMines.EMPTY).isEmpty();
	}

	@Override
	public boolean matchesSecondItem(ItemStack stack) {
		return stack.is(item) && stack.getOrDefault(SCContent.BOUND_MINES, BoundMines.EMPTY).isEmpty();
	}

	@Override
	public ItemStack combine(ItemStack itemWithPositions, ItemStack emptyItem) {
		ItemStack result = new ItemStack(item, 2);

		result.set(SCContent.BOUND_MINES, itemWithPositions.getOrDefault(SCContent.BOUND_MINES, BoundMines.EMPTY));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
