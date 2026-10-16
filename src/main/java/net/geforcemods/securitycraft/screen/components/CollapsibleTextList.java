package net.geforcemods.securitycraft.screen.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class CollapsibleTextList extends Button {
	private static final Component PLUS = Component.literal("+ ");
	private static final Component MINUS = Component.literal("- ");
	private final int initialY;
	private int threeDotsWidth;
	private int maximumHeight;
	private final Component originalDisplayString;
	private final List<List<FormattedCharSequence>> textLines = new ArrayList<>();
	private final BiPredicate<Integer, Integer> extraHoverCheck;
	private boolean open = true;
	private boolean isMessageTooLong = false;

	public CollapsibleTextList(int xPos, int yPos, int width, Component displayString, List<? extends FormattedText> textLines, OnPress onPress, BiPredicate<Integer, Integer> extraHoverCheck) {
		super(xPos, yPos, width, 12, displayString, onPress, DEFAULT_NARRATION);
		initialY = yPos;
		originalDisplayString = displayString;
		switchOpenStatus(); //properly initializes the message as well

		Font font = Minecraft.getInstance().font;
		int amountOfLines = 0;

		for (FormattedText line : textLines) {
			List<FormattedCharSequence> splitLines = font.split(line, width - 5);

			amountOfLines += splitLines.size();
			this.textLines.add(splitLines);
		}

		maximumHeight = height + amountOfLines * font.lineHeight + textLines.size() * 3;
		this.extraHoverCheck = extraHoverCheck;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (visible) {
			Font font = Minecraft.getInstance().font;
			int v = getTextureY();

			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + getHeight() && extraHoverCheck.test(mouseX, mouseY);
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			guiGraphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), getWidth(), 12, 20, 4, 200, 20, 0, v);
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			renderString(guiGraphics, font, (active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(alpha * 255.0F) << 24);

			if (open) {
				int renderedLines = 0;

				guiGraphics.fillGradient(getX(), getY() + 12, getX() + width, getY() + maximumHeight, 0xC0101010, 0xD0101010);

				for (int i = 0; i < textLines.size(); i++) {
					int textY = getY() + 2 + height + renderedLines * font.lineHeight + (i * 3);
					List<FormattedCharSequence> linesToDraw = textLines.get(i);

					if (i > 0)
						guiGraphics.fillGradient(getX() + 1, textY - 2, getX() + width - 2, textY - 1, 0xAAA0A0A0, 0xAAA0A0A0);

					for (int lineIndex = 0; lineIndex < linesToDraw.size(); lineIndex++) {
						guiGraphics.drawString(font, linesToDraw.get(lineIndex), getX() + 2, textY + lineIndex * font.lineHeight, 0xE0E0E0, false);
					}

					renderedLines += linesToDraw.size();
				}
			}
		}
	}

	private int getTextureY() {
		int y = 1;

		if (!active)
			y = 0;
		else if (isHoveredOrFocused())
			y = 2;

		return 46 + y * 20;
	}

	@Override
	public void renderString(GuiGraphics guiGraphics, Font font, int color) {
		int yOffset = (12 - 8) / 2;

		guiGraphics.drawCenteredString(font, getMessage(), getX() + font.width(getMessage()) / 2 + 3, getY() + yOffset, color);
	}

	public void renderLongMessageTooltip(GuiGraphics guiGraphics) {
		if (isMessageTooLong && isHoveredOrFocused()) {
			Minecraft mc = Minecraft.getInstance();

			guiGraphics.renderTooltip(mc.font, originalDisplayString, getX() + 1, getY() + height + 2);
		}
	}

	@Override
	public void setMessage(Component message) {
		Font font = Minecraft.getInstance().font;

		if (threeDotsWidth == 0)
			threeDotsWidth = font.width("...");

		if (font.width(message) > width - 6) { //-6 due to the 3 pixel offset on the left when rendering the message and stopping at 3 pixels from the right side
			isMessageTooLong = true;
			message = Component.literal(font.substrByWidth(message, width - 6 - threeDotsWidth).getString() + "...");
		}

		super.setMessage(message);
	}

	@Override
	public int getHeight() {
		return open ? maximumHeight : height;
	}

	public int getMaximumHeight() {
		return maximumHeight;
	}

	@Override
	public void onPress() {
		switchOpenStatus();
		super.onPress();
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return isActive() && visible && extraHoverCheck.test((int) mouseX, (int) mouseY) && mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return extraHoverCheck.test((int) mouseX, (int) mouseY) && super.isMouseOver(mouseX, mouseY);
	}

	public void setY(int y) {
		super.setY(y);
	}

	public int getInitialY() {
		return initialY;
	}

	public boolean isOpen() {
		return open;
	}

	private void switchOpenStatus() {
		open = !open;
		setMessage((open ? MINUS : PLUS).copy().append(originalDisplayString));
	}
}
