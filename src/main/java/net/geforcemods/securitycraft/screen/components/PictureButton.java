package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * A vanilla-looking button that draws a GUI sprite, or an item icon (a block item's block model, with its count
 * decoration), on top of the button background. {@code Button} is abstract on this version, so this extends its
 * plain, label-only subclass, matching upstream's own {@code Button.Plain} base.
 */
public class PictureButton extends Button.Plain {
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;
	private Identifier sprite;
	private int drawOffsetX;
	private int drawOffsetY;
	private int drawWidth;
	private int drawHeight;

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
		else
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		super.extractContents(extractor, mouseX, mouseY, partialTicks);

		if (!blockToRender.isEmpty()) {
			extractor.item(blockToRender, getX() + 2, getY() + 3);
			extractor.itemDecorations(font, blockToRender, getX() + 2, getY() + 3, "");
		}
		else if (!itemToRender.isEmpty()) {
			extractor.item(itemToRender, getX() + 2, getY() + 2);
			extractor.itemDecorations(font, itemToRender, getX() + 2, getY() + 2, "");
		}
		else if (getSpriteLocation() != null)
			extractor.blitSprite(RenderPipelines.GUI_TEXTURED, getSpriteLocation(), getX() + drawOffsetX, getY() + drawOffsetY, drawWidth, drawHeight);
	}

	public Identifier getSpriteLocation() {
		return sprite;
	}

	public ItemStack getItemStack() {
		return !blockToRender.isEmpty() ? blockToRender : itemToRender;
	}
}
