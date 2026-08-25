package net.geforcemods.securitycraft.screen;

import java.util.concurrent.ThreadLocalRandom;

import net.geforcemods.securitycraft.inventory.KeypadFurnaceMenu;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Ported 1:1 from upstream's {@code KeypadFurnaceScreen}: the vanilla furnace screen with the keypad furnace's menu. */
public class KeypadFurnaceScreen extends AbstractFurnaceScreen<KeypadFurnaceMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");
	private static final ResourceLocation LIT_PROGRESS_SPRITE = new ResourceLocation("container/furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = new ResourceLocation("container/furnace/burn_progress");

	public KeypadFurnaceScreen(KeypadFurnaceMenu menu, Inventory inv, Component title) {
		super(menu, new SmeltingRecipeBookComponent(), inv, ThreadLocalRandom.current().nextInt(100) < 5 ? Component.literal("Keypad Gurnace") : (menu.be.hasCustomName() ? menu.be.getCustomName() : title), LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TEXTURE);
	}
}
