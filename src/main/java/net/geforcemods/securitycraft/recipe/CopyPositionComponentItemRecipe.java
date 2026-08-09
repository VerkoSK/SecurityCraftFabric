package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.GlobalPositions;
import net.geforcemods.securitycraft.items.MineRemoteAccessToolItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Combines a position-storing item that has data with an empty one of the same kind, copying the stored positions
 * onto both results. 1:1 with upstream CopyPositionComponentItemRecipe, minus the factories for the items this port
 * does not have yet; the serializer is read from {@link SCContent} on demand rather than passed in, because Fabric
 * registers it eagerly and the constructor would otherwise need it before it exists.
 */
public class CopyPositionComponentItemRecipe extends CombineRecipe {
	private final Item item;
	private final DataComponentType<GlobalPositions> component;
	private final GlobalPositions defaultInstance;

	public CopyPositionComponentItemRecipe(CraftingBookCategory craftingBookCategory, Item item, DataComponentType<GlobalPositions> component, GlobalPositions defaultInstance) {
		super(craftingBookCategory);
		this.item = item;
		this.component = component;
		this.defaultInstance = defaultInstance;
	}

	public static CopyPositionComponentItemRecipe mineRemoteAccessTool(CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe(craftingBookCategory, SCContent.MINE_REMOTE_ACCESS_TOOL, SCContent.BOUND_MINES, MineRemoteAccessToolItem.DEFAULT_POSITIONS);
	}

	@Override
	public boolean matchesFirstItem(ItemStack stack) {
		return stack.is(item) && !stack.getOrDefault(component, defaultInstance).isEmpty();
	}

	@Override
	public boolean matchesSecondItem(ItemStack stack) {
		return stack.is(item) && stack.getOrDefault(component, defaultInstance).isEmpty();
	}

	@Override
	public ItemStack combine(ItemStack itemWithPositions, ItemStack emptyItem) {
		GlobalPositions positionsToCopy = itemWithPositions.getOrDefault(component, defaultInstance);
		ItemStack result = new ItemStack(item, 2);

		result.set(component, new GlobalPositions(positionsToCopy.positions()));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
