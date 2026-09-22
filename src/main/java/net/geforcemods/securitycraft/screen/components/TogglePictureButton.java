package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

/** A {@link PictureButton} that flips between two sprites (a red X / green check) each click, reporting the new state. */
public class TogglePictureButton extends PictureButton implements IToggleableButton {
	private final ResourceLocation offSprite;
	private final ResourceLocation onSprite;
	private int currentIndex;

	public TogglePictureButton(int xPos, int yPos, int width, int height, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, ResourceLocation offSprite, ResourceLocation onSprite, boolean initiallyOn, Consumer<Boolean> onToggle) {
		super(xPos, yPos, width, height, offSprite, drawOffsetX, drawOffsetY, drawWidth, drawHeight, b -> {
			TogglePictureButton self = (TogglePictureButton) b;

			self.currentIndex = 1 - self.currentIndex;
			onToggle.accept(self.currentIndex == 1);
		});
		this.offSprite = offSprite;
		this.onSprite = onSprite;
		currentIndex = initiallyOn ? 1 : 0;
	}

	@Override
	public ResourceLocation getSpriteLocation() {
		return currentIndex == 1 ? onSprite : offSprite;
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void setCurrentIndex(int newIndex) {
		currentIndex = newIndex;
	}
}
