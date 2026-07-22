package net.geforcemods.securitycraft.screen.components;

import net.minecraft.resources.Identifier;

/** A {@link PictureButton} that shows a different sprite when inactive. */
public class ActiveBasedTextureButton extends PictureButton {
	private final Identifier inactiveSprite;

	public ActiveBasedTextureButton(int xPos, int yPos, int width, int height, Identifier sprite, Identifier inactiveSprite, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, sprite, drawOffsetX, drawOffsetY, drawWidth, drawHeight, onPress);
		this.inactiveSprite = inactiveSprite;
	}

	@Override
	public Identifier getSpriteLocation() {
		return active ? super.getSpriteLocation() : inactiveSprite;
	}
}
