package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TogglePictureButton extends Button implements IToggleableButton {
	private ResourceLocation textureLocation;
	private int[] u;
	private int[] v;
	private int currentIndex = 0;
	private final int toggleCount;
	private final int drawOffset;
	private final int drawWidth;
	private final int drawHeight;
	private final int uWidth;
	private final int vHeight;
	private final int textureWidth;
	private final int textureHeight;
	private final ResourceLocation offSprite;
	private final ResourceLocation onSprite;

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
		this.offSprite = null;
		this.onSprite = null;
	}

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, ResourceLocation offSprite, ResourceLocation onSprite, boolean initiallyOn, Consumer<Boolean> onToggle) {
		super(xPos, yPos, width, height, Component.empty(), b -> {
			TogglePictureButton self = (TogglePictureButton) b;

			self.currentIndex = 1 - self.currentIndex;
			onToggle.accept(self.currentIndex == 1);
		}, DEFAULT_NARRATION);
		this.offSprite = offSprite;
		this.onSprite = onSprite;
		this.currentIndex = initiallyOn ? 1 : 0;
		this.toggleCount = 2;
		this.drawOffset = drawOffsetX;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.uWidth = 0;
		this.vHeight = 0;
		this.textureWidth = 0;
		this.textureHeight = 0;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
			guiGraphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), width, height, 20, 4, 200, 20, 0, 46 + (!active ? 0 : (isHovered ? 40 : 20)));

			if (offSprite != null && onSprite != null) {
				guiGraphics.blit(currentIndex == 1 ? onSprite : offSprite, getX() + drawOffset, getY() + drawOffset, 0.0F, 0.0F, drawWidth, drawHeight, drawWidth, drawHeight);
			}
			else if (getTextureLocation() != null) {
				guiGraphics.blit(getTextureLocation(), getX() + drawOffset, getY() + drawOffset, drawWidth, drawHeight, u[currentIndex], v[currentIndex], uWidth, vHeight, textureWidth, textureHeight);
			}
		}
	}

	@Override
	public void onPress() {
		if (offSprite == null && onSprite == null) {
			if (Screen.hasShiftDown())
				setCurrentIndex(currentIndex - 1);
			else
				setCurrentIndex(currentIndex + 1);
		}

		super.onPress();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (offSprite == null && onSprite == null) {
			setCurrentIndex(currentIndex - (int) Math.signum(delta));
			onPress();
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void setCurrentIndex(int newIndex) {
		currentIndex = Math.floorMod(newIndex, toggleCount);
	}

	public ResourceLocation getTextureLocation() {
		return textureLocation;
	}
}
