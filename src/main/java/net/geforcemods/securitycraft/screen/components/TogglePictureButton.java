package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TogglePictureButton extends Button implements IToggleableButton {
	private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted"));
	private final Identifier[] sprites;
	private final Identifier textureLocation;
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

	public TogglePictureButton(int xPos, int yPos, int width, int height, Identifier texture, int[] textureX, int[] textureY, int drawOffset, int toggleCount, OnPress onPress) {
		this(xPos, yPos, width, height, texture, textureX, textureY, drawOffset, 16, 16, 16, 16, 256, 256, toggleCount, onPress);
	}

	public TogglePictureButton(int xPos, int yPos, int width, int height, Identifier texture, int[] textureX, int[] textureY, int drawOffset, int drawWidth, int drawHeight, int uWidth, int vHeight, int textureWidth, int textureHeight, int toggleCount, OnPress onPress) {
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

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffset, int drawWidth, int drawHeight, int toggleCount, OnPress onPress, Identifier... sprites) {
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

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, Identifier offSprite, Identifier onSprite, boolean initiallyOn, java.util.function.Consumer<Boolean> onToggle) {
		super(xPos, yPos, width, height, Component.empty(), b -> {
			TogglePictureButton button = (TogglePictureButton) b;
			button.setCurrentIndex(1 - button.getCurrentIndex());
			onToggle.accept(button.getCurrentIndex() == 1);
		}, DEFAULT_NARRATION);

		this.sprites = new Identifier[]{offSprite, onSprite};
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
	protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		extractDefaultSprite(extractor);
		extractDefaultLabel(extractor.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));

		if (sprites != null && sprites.length > 0) {
			Identifier sprite = getCurrentSprite();

			if (sprite != null)
				extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX() + drawOffset, getY() + drawOffset, drawWidth, drawHeight);
		}
		else if (textureLocation != null && u != null && v != null) {
			extractor.blit(RenderPipelines.GUI_TEXTURED, textureLocation, getX() + drawOffset, getY() + drawOffset, (float) u[currentIndex], (float) v[currentIndex], drawWidth, drawHeight, textureWidth, textureHeight);
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

	public Identifier getCurrentSprite() {
		return sprites != null && currentIndex < sprites.length ? sprites[getCurrentIndex()] : null;
	}
}
