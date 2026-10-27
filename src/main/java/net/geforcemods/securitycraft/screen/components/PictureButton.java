package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A vanilla-looking button that draws a GUI sprite, a texture subregion or an item icon on top of the button background. */
public class PictureButton extends Button {
	private static final Identifier BUTTON_SPRITE = Identifier.withDefaultNamespace("widget/button");
	private static final Identifier BUTTON_DISABLED_SPRITE = Identifier.withDefaultNamespace("widget/button_disabled");
	private static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/button_highlighted");
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;
	private Identifier sprite;
	private int u;
	private int v;
	private int drawOffsetX;
	private int drawOffsetY;
	private int drawWidth;
	private int drawHeight;
	private int textureWidth;
	private int textureHeight;

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

		if (!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = new ItemStack(Block.byItem(itemToRender.getItem()));
		else if (!itemToRender.isEmpty())
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	/** Draws a {@code drawWidth}x{@code drawHeight} subregion at (textureX, textureY) of a sheet. 1:1 with the upstream constructor of the same shape. */
	public PictureButton(int xPos, int yPos, int width, int height, Identifier texture, int textureX, int textureY, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, int textureWidth, int textureHeight, OnPress onPress) {
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
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderDefaultSprite(guiGraphics);
		renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));

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
			if (textureWidth > 0)
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, (float) u, (float) v, drawWidth, drawHeight, textureWidth, textureHeight);
			else
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
		}
	}

	public Identifier getSpriteLocation() {
		return sprite;
	}
}