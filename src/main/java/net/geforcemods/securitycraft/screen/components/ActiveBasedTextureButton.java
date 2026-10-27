package net.geforcemods.securitycraft.screen.components;

import net.minecraft.resources.Identifier;

public class ActiveBasedTextureButton extends PictureButton {
	private final Identifier inactiveTexture;

	public ActiveBasedTextureButton(int xPos, int yPos, int width, int height, Identifier activeTexture, Identifier inactiveTexture, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, OnPress onPress) {
		super(xPos, yPos, width, height, activeTexture, drawOffsetX, drawOffsetY, drawWidth, drawHeight, onPress);
		this.inactiveTexture = inactiveTexture;
	}

	public ActiveBasedTextureButton(int xPos, int yPos, int width, int height, Identifier activeTexture, Identifier inactiveTexture, int textureX, int textureY, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, int textureWidth, int textureHeight, OnPress onPress) {
		super(xPos, yPos, width, height, activeTexture, textureX, textureY, drawOffsetX, drawOffsetY, drawWidth, drawHeight, textureWidth, textureHeight, onPress);
		this.inactiveTexture = inactiveTexture;
	}

	@Override
	public Identifier getSpriteLocation() {
		return isActive() ? super.getSpriteLocation() : inactiveTexture;
	}
}
