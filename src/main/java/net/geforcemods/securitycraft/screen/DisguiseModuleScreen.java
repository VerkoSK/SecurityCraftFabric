package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.inventory.DisguiseModuleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DisguiseModuleScreen extends AbstractContainerScreen<DisguiseModuleMenu> {
	public DisguiseModuleScreen(DisguiseModuleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		imageHeight = 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);

		for (Slot slot : menu.slots) {
			int sx = leftPos + slot.x;
			int sy = topPos + slot.y;

			guiGraphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
			guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}
}
