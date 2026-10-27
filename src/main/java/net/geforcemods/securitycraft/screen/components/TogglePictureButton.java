package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.input.InputWithModifiers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TogglePictureButton extends Button implements IToggleableButton {
	private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
	private final ResourceLocation[] sprites;
	private final ResourceLocation textureLocation;
	private final int[] u;
	private final int[] v;
	private final int uWidth;
	private final int vHeight;
	private final int textureWidth;
	private final int textureHeight;
	private final int toggleCount;
	private final int drawOffset;
	private final int drawWidth;
	private final int drawHeight;
	private int currentIndex = 0;

	public TogglePictureButton(int xPos, int yPos, int width, int height, ResourceLocation texture, int[] textureX, int[] textureY, int drawOffset, int toggleCount, OnPress onPress) {
		this(xPos, yPos, width, height, texture, textureX, textureY, drawOffset, 16, 16, 16, 16, 256, 256, toggleCount, onPress);
	}

	public TogglePictureButton(int xPos, int yPos, int width, int height, ResourceLocation texture, int[] textureX, int[] textureY, int drawOffset, int drawWidth, int drawHeight, int uWidth, int vHeight, int textureWidth, int textureHeight, int toggleCount, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		if (textureX.length != toggleCount || textureY.length != toggleCount)
			throw new IllegalArgumentException("TogglePictureButton was set up incorrectly. Array lengths must match toggleCount!");

		this.textureLocation = texture;
		this.u = textureX;
		this.v = textureY;
		this.toggleCount = toggleCount;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.drawOffset = drawOffset;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.sprites = null;
	}

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffset, int drawWidth, int drawHeight, int toggleCount, OnPress onPress, ResourceLocation... sprites) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		if (sprites.length != toggleCount)
			throw new IllegalArgumentException("TogglePictureButton was set up incorrectly. Amount of sprites must match toggleCount!");

		this.sprites = sprites;
		this.toggleCount = toggleCount;
		this.drawOffset = drawOffset;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.textureLocation = null;
		this.u = null;
		this.v = null;
		this.uWidth = 0;
		this.vHeight = 0;
		this.textureWidth = 0;
		this.textureHeight = 0;
	}

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, ResourceLocation offSprite, ResourceLocation onSprite, boolean initiallyOn, java.util.function.Consumer<Boolean> onToggle) {
		super(xPos, yPos, width, height, Component.empty(), b -> {
			TogglePictureButton button = (TogglePictureButton) b;
			button.setCurrentIndex(1 - button.getCurrentIndex());
			onToggle.accept(button.getCurrentIndex() == 1);
		}, DEFAULT_NARRATION);

		this.sprites = new ResourceLocation[]{offSprite, onSprite};
		this.toggleCount = 2;
		this.drawOffset = drawOffsetX;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.currentIndex = initiallyOn ? 1 : 0;
		this.textureLocation = null;
		this.u = null;
		this.v = null;
		this.uWidth = 0;
		this.vHeight = 0;
		this.textureWidth = 0;
		this.textureHeight = 0;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight());

			if (sprites != null && sprites.length > 0) {
				ResourceLocation sprite = getCurrentSprite();

				if (sprite != null)
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX() + drawOffset, getY() + drawOffset, drawWidth, drawHeight);
			}
			else if (textureLocation != null && u != null && v != null) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureLocation, getX() + drawOffset, getY() + drawOffset, u[currentIndex], v[currentIndex], drawWidth, drawHeight, textureWidth, textureHeight);
			}
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (net.minecraft.client.Minecraft.getInstance().hasShiftDown())
			setCurrentIndex(currentIndex - 1);
		else
			setCurrentIndex(currentIndex + 1);

		super.onPress(input);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		setCurrentIndex(currentIndex - (int) Math.signum(scrollY));
		onPress.onPress(this);
		return true;
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void setCurrentIndex(int newIndex) {
		currentIndex = Math.floorMod(newIndex, toggleCount);
	}

	public ResourceLocation getCurrentSprite() {
		return sprites != null && currentIndex < sprites.length ? sprites[getCurrentIndex()] : null;
	}
}
