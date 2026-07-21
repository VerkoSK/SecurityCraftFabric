package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.inventory.DisguiseModuleMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DisguiseModuleScreen extends AbstractContainerScreen<DisguiseModuleMenu> {
	public DisguiseModuleScreen(DisguiseModuleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		extractor.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);

		for (Slot slot : menu.slots) {
			int sx = leftPos + slot.x;
			int sy = topPos + slot.y;

			extractor.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
			extractor.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
		}

		super.extractRenderState(extractor, mouseX, mouseY, partialTick);
	}
}
