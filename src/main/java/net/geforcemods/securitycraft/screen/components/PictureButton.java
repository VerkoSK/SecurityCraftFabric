package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A vanilla-looking button that draws a GUI sprite on top of the button background. */
public class PictureButton extends Button {
	private final ResourceLocation sprite;
	private final int drawOffsetX;
	private final int drawOffsetY;
	private final int drawWidth;
	private final int drawHeight;

	public PictureButton(int xPos, int yPos, int width, int height, ResourceLocation sprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.sprite = sprite;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;

		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

		if (getSpriteLocation() != null)
			guiGraphics.blitSprite(getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
	}

	public ResourceLocation getSpriteLocation() {
		return sprite;
	}
}
