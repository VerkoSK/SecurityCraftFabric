package net.geforcemods.securitycraft.screen;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.geforcemods.securitycraft.inventory.KeypadFurnaceMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

/** Ported 1:1 from upstream's {@code KeypadFurnaceScreen}: the vanilla furnace screen with the keypad furnace's menu. */
public class KeypadFurnaceScreen extends AbstractFurnaceScreen<KeypadFurnaceMenu> {
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
	private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
			new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),
			new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
			new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
			new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC));

	public KeypadFurnaceScreen(KeypadFurnaceMenu menu, Inventory inv, Component title) {
		super(menu, inv, ThreadLocalRandom.current().nextInt(100) < 5 ? Component.literal("Keypad Gurnace") : (menu.be.getCustomName() != null ? menu.be.getCustomName() : title), FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}
}
