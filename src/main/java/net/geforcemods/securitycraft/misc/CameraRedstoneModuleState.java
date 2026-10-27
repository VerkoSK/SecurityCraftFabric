package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum CameraRedstoneModuleState {
	NOT_INSTALLED((guiGraphics, x, y) -> guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SecurityCraftClient.CAMERA_DASHBOARD, x, y, 104, 0, 12, 12, 256, 256)),
	DEACTIVATED((guiGraphics, x, y) -> guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SecurityCraftClient.CAMERA_DASHBOARD, x, y, 90, 0, 12, 12, 256, 256)),
	ACTIVATED((guiGraphics, x, y) -> guiGraphics.renderItem(new ItemStack(Items.REDSTONE), x - 2, y - 2));

	@FunctionalInterface
	public interface RenderFunction {
		void accept(GuiGraphics guiGraphics, int x, int y);
	}

	private final RenderFunction renderer;

	CameraRedstoneModuleState(RenderFunction renderer) {
		this.renderer = renderer;
	}

	public void render(GuiGraphics guiGraphics, int x, int y) {
		renderer.accept(guiGraphics, x, y);
	}
}
