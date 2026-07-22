package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** A checkbox with a callback + custom text colour. Drawn without GUI sprites (which don't exist on MC 1.20.1). */
public class CallbackCheckbox extends AbstractButton {
	private boolean selected;
	private final Consumer<Boolean> onChange;
	private final int textColor;

	public CallbackCheckbox(int x, int y, int width, int height, Component message, boolean selected, Consumer<Boolean> onChange, int textColor) {
		super(x, y, width, height, message);
		this.selected = selected;
		this.onChange = onChange;
		this.textColor = textColor;
	}

	@Override
	public void onPress() {
		setSelected(!selected);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		int boxSize = Math.min(width, height);

		guiGraphics.fill(getX(), getY(), getX() + boxSize, getY() + boxSize, 0xFF000000);
		guiGraphics.fill(getX() + 1, getY() + 1, getX() + boxSize - 1, getY() + boxSize - 1, isFocused() ? 0xFF666666 : 0xFF3C3C3C);

		if (selected)
			guiGraphics.fill(getX() + 3, getY() + 3, getX() + boxSize - 3, getY() + boxSize - 3, 0xFFE0E0E0);

		guiGraphics.drawString(minecraft.font, getMessage(), getX() + (int) (boxSize * 1.2F), getY() + (boxSize - 8) / 2, 0xFF000000 | textColor, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());

		if (active) {
			if (isFocused())
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
			else
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		onChange.accept(selected);
	}

	public boolean selected() {
		return selected;
	}
}
