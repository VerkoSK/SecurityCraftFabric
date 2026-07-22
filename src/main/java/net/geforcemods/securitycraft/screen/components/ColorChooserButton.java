package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Button that toggles the {@link ColorChooser} layer and previews the current colour. Fabric has no
 * gui-layer stack, so the parent screen embeds the chooser and reacts to {@code onToggle}.
 */
public class ColorChooserButton extends Button {
	private final ColorChooser colorChooser;
	private final Runnable onToggle;

	public ColorChooserButton(int x, int y, int width, int height, ColorChooser colorChooser, Runnable onToggle) {
		super(x, y, width, height, Component.empty(), b -> {}, DEFAULT_NARRATION);
		this.colorChooser = colorChooser;
		this.onToggle = onToggle;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		int color = colorChooser.getARGBColor();

		extractDefaultSprite(extractor);
		extractor.fill(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2, color);
	}

	@Override
	public void onPress(InputWithModifiers input) {
		colorChooser.disabled = !colorChooser.disabled;
		onToggle.run();
	}
}
