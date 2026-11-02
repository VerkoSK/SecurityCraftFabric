package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum CameraRedstoneModuleState {
	NOT_INSTALLED((extractor, x, y) -> extractor.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SecurityCraftClient.CAMERA_DASHBOARD, x, y, 104.0F, 0.0F, 12, 12, 256, 256)),
	DEACTIVATED((extractor, x, y) -> extractor.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SecurityCraftClient.CAMERA_DASHBOARD, x, y, 90.0F, 0.0F, 12, 12, 256, 256)),
	ACTIVATED((extractor, x, y) -> extractor.item(new ItemStack(Items.REDSTONE), x - 2, y - 2));

	@FunctionalInterface
	public interface RenderFunction {
		void accept(GuiGraphicsExtractor extractor, int x, int y);
	}

	private final RenderFunction renderer;

	CameraRedstoneModuleState(RenderFunction renderer) {
		this.renderer = renderer;
	}

	public void render(GuiGraphicsExtractor extractor, int x, int y) {
		renderer.accept(extractor, x, y);
	}
}
