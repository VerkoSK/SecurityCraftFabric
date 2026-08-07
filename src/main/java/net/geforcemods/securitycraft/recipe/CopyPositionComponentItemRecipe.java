package net.geforcemods.securitycraft.recipe;

import java.util.function.Predicate;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.MineRemoteAccessToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
	private final Predicate<CompoundTag> isDataEmpty;

	public CopyPositionComponentItemRecipe(ResourceLocation id, CraftingBookCategory craftingBookCategory, Item item, Predicate<CompoundTag> hasData) {
		super(id, craftingBookCategory);
		this.item = item;
		isDataEmpty = Predicate.not(hasData);
	}

	public static CopyPositionComponentItemRecipe mineRemoteAccessTool(ResourceLocation id, CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe(id, craftingBookCategory, SCContent.MINE_REMOTE_ACCESS_TOOL, MineRemoteAccessToolItem::hasMineAdded);
	}

	@Override
	public boolean matchesFirstItem(ItemStack stack) {
		return stack.is(item) && !isDataEmpty.test(stack.getOrCreateTag());
	}

	@Override
	public boolean matchesSecondItem(ItemStack stack) {
		return stack.is(item) && isDataEmpty.test(stack.getOrCreateTag());
	}

	@Override
	public ItemStack combine(ItemStack itemWithPositions, ItemStack emptyItem) {
		ItemStack result = new ItemStack(item, 2);

		result.getOrCreateTag().merge(itemWithPositions.getOrCreateTag());
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	}
}
