package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A vanilla-looking button that draws a GUI sprite, or an item icon, on top of the button background. */
public class PictureButton extends Button {
	private final Identifier sprite;
	private final int drawOffsetX;
	private final int drawOffsetY;
	private final int drawWidth;
	private final int drawHeight;
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;

	public PictureButton(int xPos, int yPos, int width, int height, Identifier sprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.sprite = sprite;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
	}

	/** Draws the given item's icon (a block item's block model) on the button. 1:1 with the upstream constructor of the same shape. */
	public PictureButton(int xPos, int yPos, int width, int height, ItemStack itemToRender, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		sprite = null;
		drawOffsetX = 0;
		drawOffsetY = 0;
		drawWidth = 0;
		drawHeight = 0;

		if (!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = new ItemStack(Block.byItem(itemToRender.getItem()));
		else if (!itemToRender.isEmpty())
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	@Override
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderDefaultSprite(guiGraphics);
		renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));

		if (!blockToRender.isEmpty())
			guiGraphics.renderItem(blockToRender, getX() + 2, getY() + 3);
		else if (!itemToRender.isEmpty())
			guiGraphics.renderItem(itemToRender, getX() + 2, getY() + 2);
		else if (getSpriteLocation() != null)
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
	}

	public Identifier getSpriteLocation() {
		return sprite;
	}
}
