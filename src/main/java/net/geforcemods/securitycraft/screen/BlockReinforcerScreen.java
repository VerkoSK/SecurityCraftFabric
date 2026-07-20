package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.inventory.BlockReinforcerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Screen for the Universal Block Reinforcer/Remover container. */
public class BlockReinforcerScreen extends AbstractContainerScreen<BlockReinforcerMenu> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("securitycraft", "textures/gui/container/universal_block_reinforcer.png");

	public BlockReinforcerScreen(BlockReinforcerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageHeight = 186;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;

		guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
		guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
		guiGraphics.drawString(font, Component.translatable(menu.isReinforcing() ? "gui.securitycraft:reinforcer.reinforcing" : "gui.securitycraft:reinforcer.removing"), 8, 42, 0x404040, false);
	}
}
