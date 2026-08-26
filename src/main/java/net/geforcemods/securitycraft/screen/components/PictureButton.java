package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A vanilla-looking button that draws a GUI sprite, a texture subregion or an item icon on top of the button background. */
public class PictureButton extends Button {
	//adaptation: vanilla's button background moved from one nine-sliced WIDGETS_LOCATION texture to three separate sprites
	private static final ResourceLocation BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("widget/button");
	private static final ResourceLocation BUTTON_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("widget/button_disabled");
	private static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;
	private ResourceLocation sprite;
	private int u;
	private int v;
	private int drawOffsetX;
	private int drawOffsetY;
	private int drawWidth;
	private int drawHeight;
	private int textureWidth;
	private int textureHeight;

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

	/** Draws a {@code drawWidth}x{@code drawHeight} subregion at (textureX, textureY) of a sheet. 1:1 with the upstream constructor of the same shape. */
	public PictureButton(int xPos, int yPos, int width, int height, ResourceLocation texture, int textureX, int textureY, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, int textureWidth, int textureHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		sprite = texture;
		u = textureX;
		v = textureY;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;

		//upstream draws the button background itself instead of letting vanilla do it. Doing the same here is what
		//keeps the highlighted background tied to the mouse: vanilla picks it for isHoveredOrFocused(), so a button
		//that was clicked once stays lit for as long as it keeps the focus.
		isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
		guiGraphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, !active ? BUTTON_DISABLED_SPRITE : (isHovered ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE), getX(), getY(), width, height);

		if (!blockToRender.isEmpty()) {
			Font font = Minecraft.getInstance().font;

			guiGraphics.renderItem(blockToRender, getX() + 2, getY() + 3);
			guiGraphics.renderItemDecorations(font, blockToRender, getX() + 2, getY() + 3, "");
		}
		else if (!itemToRender.isEmpty()) {
			Font font = Minecraft.getInstance().font;

			guiGraphics.renderItem(itemToRender, getX() + 2, getY() + 2);
			guiGraphics.renderItemDecorations(font, itemToRender, getX() + 2, getY() + 2, "");
		}
		else if (getSpriteLocation() != null) {
			//a texture width means the location points at a raw texture and a subregion of it should be drawn, otherwise it is a GUI sprite
			if (textureWidth > 0)
				guiGraphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, u, v, drawWidth, drawHeight, drawWidth, drawHeight, textureWidth, textureHeight);
			else
				guiGraphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
		}
	}

	public ResourceLocation getSpriteLocation() {
		return sprite;
	}
}
