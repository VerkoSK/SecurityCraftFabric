package net.geforcemods.securitycraft.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SmallButton extends Button {
	protected SmallButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
		super(xPos, yPos, width, height, displayString, handler, DEFAULT_NARRATION);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		guiGraphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), getWidth(), getHeight(), 20, 4, 200, 20, 0, getTextureY());
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		renderString(guiGraphics, minecraft.font, (active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(alpha * 255.0F) << 24);
	}

	private int getTextureY() {
		int y = 1;

		if (!active)
			y = 0;
		else if (isHoveredOrFocused())
			y = 2;

		return 46 + y * 20;
	}

	public static SmallButton create(int xPos, int yPos, Component displayString, OnPress handler) {
		return new SmallButton(xPos, yPos, 8, 8, displayString, handler);
	}

	public static SmallButton createWithX(int xPos, int yPos, OnPress handler) {
		return new SmallButton(xPos, yPos, 8, 8, Component.literal("x"), handler) {
			@Override
			public void renderString(GuiGraphics guiGraphics, Font font, int color) {
				int xOffset = getX() + width / 2;
				int yOffset = getY() + (height - 7) / 2;

				guiGraphics.drawCenteredString(font, getMessage(), xOffset, yOffset, color);
			}
		};
	}
}
