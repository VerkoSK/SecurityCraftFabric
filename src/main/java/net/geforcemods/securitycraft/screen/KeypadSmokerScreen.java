package net.geforcemods.securitycraft.screen;

import java.util.List;

import net.geforcemods.securitycraft.inventory.KeypadSmokerMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

/** Ported 1:1 from upstream's {@code KeypadSmokerScreen}: the vanilla smoker screen with the keypad smoker's menu. */
public class KeypadSmokerScreen extends AbstractFurnaceScreen<KeypadSmokerMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/smoker.png");
	private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/smoker/lit_progress");
	private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/smoker/burn_progress");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smokable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
			new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.SMOKER),
			new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.SMOKER_FOOD));

	public KeypadSmokerScreen(KeypadSmokerMenu menu, Inventory inv, Component title) {
		super(menu, inv, menu.be.getCustomName() != null ? menu.be.getCustomName() : title, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}
}
