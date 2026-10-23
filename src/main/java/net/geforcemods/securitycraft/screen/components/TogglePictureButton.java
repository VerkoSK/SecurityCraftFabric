package net.geforcemods.securitycraft.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TogglePictureButton extends Button implements IToggleableButton {
	private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(new ResourceLocation("widget/button"), new ResourceLocation("widget/button_disabled"), new ResourceLocation("widget/button_highlighted"));
	private final ResourceLocation[] sprites;
	private final int toggleCount;
	private final int drawOffset;
	private final int drawWidth;
	private final int drawHeight;
	private int currentIndex = 0;

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffset, int drawWidth, int drawHeight, int toggleCount, OnPress onPress, ResourceLocation... sprites) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

		if (sprites.length != toggleCount)
			throw new IllegalArgumentException("TogglePictureButton was set up incorrectly. Amount of sprites must match toggleCount!");

		this.sprites = sprites;
		this.toggleCount = toggleCount;
		this.drawOffset = drawOffset;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
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
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			ResourceLocation sprite = getCurrentSprite();

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
			guiGraphics.blitSprite(BUTTON_SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight());

			if (sprite != null)
				guiGraphics.blitSprite(sprite, getX() + drawOffset, getY() + drawOffset, drawWidth, drawHeight);
		}
	}

	@Override
	public void onPress() {
		if (Screen.hasShiftDown())
			setCurrentIndex(currentIndex - 1);
		else
			setCurrentIndex(currentIndex + 1);

		super.onPress();
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
		return sprites[getCurrentIndex()];
	}
}
