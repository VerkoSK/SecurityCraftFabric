package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.inventory.KeypadBlastFurnaceMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.BlastingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Ported 1:1 from upstream's {@code KeypadBlastFurnaceScreen}: the vanilla blast furnace screen with the keypad blast furnace's menu. */
public class KeypadBlastFurnaceScreen extends AbstractFurnaceScreen<KeypadBlastFurnaceMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/blast_furnace.png");

	public KeypadBlastFurnaceScreen(KeypadBlastFurnaceMenu menu, Inventory inv, Component title) {
		super(menu, new BlastingRecipeBookComponent(), inv, menu.be.hasCustomName() ? menu.be.getCustomName() : title, TEXTURE);
	}
}
