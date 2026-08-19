package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A vanilla-looking button that draws a GUI sprite or an item icon on top of the button background. */
public class PictureButton extends Button {
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;
	private ResourceLocation sprite;
	private int drawOffsetX;
	private int drawOffsetY;
	private int drawWidth;
	private int drawHeight;

	public PictureButton(int xPos, int yPos, int width, int height, ResourceLocation sprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
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

		if (!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = new ItemStack(Block.byItem(itemToRender.getItem()));
		else
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;

		//upstream draws the button background itself instead of letting vanilla do it. Doing the same here is what
		//keeps the highlighted background tied to the mouse: vanilla picks it for isHoveredOrFocused(), so a button
		//that was clicked once stays lit for as long as it keeps the focus.
		isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
		guiGraphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), width, height, 20, 4, 200, 20, 0, 46 + (!active ? 0 : (isHovered ? 40 : 20)));

		Font font = Minecraft.getInstance().font;

		if (!blockToRender.isEmpty()) {
			guiGraphics.renderItem(blockToRender, getX() + 2, getY() + 3);
			guiGraphics.renderItemDecorations(font, blockToRender, getX() + 2, getY() + 3, "");
		}
		else if (!itemToRender.isEmpty()) {
			guiGraphics.renderItem(itemToRender, getX() + 2, getY() + 2);
			guiGraphics.renderItemDecorations(font, itemToRender, getX() + 2, getY() + 2, "");
		}
		else if (getSpriteLocation() != null)
			guiGraphics.blitSprite(RenderType::guiTextured, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
	}

	public ResourceLocation getSpriteLocation() {
		return sprite;
	}
}
