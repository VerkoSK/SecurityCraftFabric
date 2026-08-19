package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * A button whose label can be drawn in an arbitrary colour. Fabric stand-in for the {@code setFGColor} that
 * upstream gets from Forge's widget patches, which is what makes an option button that still holds its default
 * value show up in pale yellow.
 */
public class ColoredButton extends Button {
	private int fgColor = 0xFFFFFF;

	public ColoredButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
	}

	public void setFGColor(int fgColor) {
		this.fgColor = fgColor;
	}

	@Override
	public void renderString(GuiGraphics guiGraphics, Font font, int color) {
		super.renderString(guiGraphics, font, (active ? fgColor : 0xA0A0A0) | Mth.ceil(alpha * 255.0F) << 24);
	}
}
