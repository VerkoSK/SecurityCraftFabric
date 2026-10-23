package net.geforcemods.securitycraft.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PictureButton extends Button {
	private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
	protected ItemStack blockToRender = ItemStack.EMPTY;
	protected ItemStack itemToRender = ItemStack.EMPTY;
	protected ResourceLocation sprite;
	protected int u;
	protected int v;
	protected int drawOffsetX;
	protected int drawOffsetY;
	protected int drawWidth;
	protected int drawHeight;
	protected int textureWidth;
	protected int textureHeight;

	public PictureButton(int xPos, int yPos, int width, int height, ItemStack itemToRender) {
		this(xPos, yPos, width, height, itemToRender, b -> {});
	}

	public PictureButton(int xPos, int yPos, int width, int height, ItemStack itemToRender, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		if (!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = new ItemStack(Block.byItem(itemToRender.getItem()));
		else
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	public PictureButton(int xPos, int yPos, int width, int height, ResourceLocation sprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		this.sprite = sprite;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
	}

	public PictureButton(int xPos, int yPos, int width, int height, ResourceLocation texture, int textureX, int textureY, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, int textureWidth, int textureHeight, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		this.sprite = texture;
		this.u = textureX;
		this.v = textureY;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			Font font = mc.font;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
			guiGraphics.blitSprite(BUTTON_SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight());

			if (!blockToRender.isEmpty()) {
				guiGraphics.renderItem(blockToRender, getX() + 2, getY() + 3);
				guiGraphics.renderItemDecorations(font, blockToRender, getX() + 2, getY() + 3, "");
			}
			else if (!itemToRender.isEmpty()) {
				guiGraphics.renderItem(itemToRender, getX() + 2, getY() + 2);
				guiGraphics.renderItemDecorations(font, itemToRender, getX() + 2, getY() + 2, "");
			}
			else if (getSpriteLocation() != null) {
				if (textureWidth > 0)
					guiGraphics.blit(getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight, u, v, drawWidth, drawHeight, textureWidth, textureHeight);
				else
					guiGraphics.blitSprite(getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
			}
		}
	}

	public ResourceLocation getSpriteLocation() {
		return sprite;
	}

	public ItemStack getItemStack() {
		return !blockToRender.isEmpty() ? blockToRender : itemToRender;
	}
}
