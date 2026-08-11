package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.inventory.BlockReinforcerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Screen for the Universal Block Reinforcer/Remover container. */
public class BlockReinforcerScreen extends AbstractContainerScreen<BlockReinforcerMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("securitycraft", "textures/gui/container/universal_block_reinforcer.png");

	public BlockReinforcerScreen(BlockReinforcerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 186);
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
		extractor.text(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
		extractor.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
		extractor.text(font, Component.translatable(menu.isReinforcing() ? "gui.securitycraft:reinforcer.reinforcing" : "gui.securitycraft:reinforcer.removing"), 8, 42, 0xFF404040, false);
	}
}
