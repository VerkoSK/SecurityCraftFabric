package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

public class SmallButton extends Button {
	private SmallButton(int xPos, int yPos, Component text, OnPress handler) {
		super(xPos, yPos, 8, 8, text, handler, DEFAULT_NARRATION);
	}

	public static SmallButton create(int xPos, int yPos, Component text, OnPress handler) {
		return new SmallButton(xPos, yPos, text, handler);
	}

	public static SmallButton createWithX(int xPos, int yPos, OnPress handler) {
		return new SmallButton(xPos, yPos, Component.literal("x"), handler);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		extractDefaultSprite(extractor);
		Font font = net.minecraft.client.Minecraft.getInstance().font;
		int color = active ? (isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFFFFFFFF) : 0xFFA0A0A0;
		extractor.centeredText(font, Language.getInstance().getVisualOrder(getMessage()), getX() + width / 2, getY() + (height - 8) / 2, color);
	}
}
