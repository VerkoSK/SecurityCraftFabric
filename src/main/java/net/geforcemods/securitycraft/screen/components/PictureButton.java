package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** A vanilla-looking button that draws a GUI sprite, or an item icon, on top of the button background. */
public class PictureButton extends Button {
	private final Identifier sprite;
	private final int drawOffsetX;
	private final int drawOffsetY;
	private final int drawWidth;
	private final int drawHeight;
	private final ItemStack itemIcon;

	public PictureButton(int xPos, int yPos, int width, int height, Identifier sprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.sprite = sprite;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		itemIcon = ItemStack.EMPTY;
	}

	/** Draws an item's icon, centered, on top of the button background instead of a sprite. */
	public PictureButton(int xPos, int yPos, int width, int height, ItemStack itemIcon, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		sprite = null;
		drawOffsetX = 0;
		drawOffsetY = 0;
		drawWidth = 0;
		drawHeight = 0;
		this.itemIcon = itemIcon;
	}

	@Override
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderDefaultSprite(guiGraphics);
		renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));

		if (getSpriteLocation() != null)
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
		else if (!itemIcon.isEmpty())
			guiGraphics.renderItem(itemIcon, getX() + (width - 16) / 2, getY() + (height - 16) / 2);
	}

	public Identifier getSpriteLocation() {
		return sprite;
	}
}
