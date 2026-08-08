package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.BoundMines;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Combines a position-storing item that has data with an empty one of the same kind, copying the stored positions onto
 * both results. 1:1 with upstream CopyPositionComponentItemRecipe, minus the factories for the items this port does not
 * have yet; the serializer is read from {@link SCContent} on demand rather than passed in, because Fabric registers it
 * eagerly and the constructor would otherwise need it before it exists.
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
		BoundMines positionsToCopy = itemWithPositions.getOrDefault(SCContent.BOUND_MINES, BoundMines.EMPTY);
		ItemStack result = new ItemStack(item, 2);

		result.set(SCContent.BOUND_MINES, new BoundMines(positionsToCopy.positions()));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
