package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * A button whose label can be drawn in an arbitrary colour. Fabric stand-in for the {@code setFGColor} that
 * upstream gets from Forge's widget patches, which is what makes an option button that still holds its default
 * value show up in pale yellow. {@code Button} itself is abstract on this version, so this extends its plain,
 * label-only subclass instead, and recolours the label by overriding {@link #getMessage()} - the widget's label
 * extraction reads the message's own style, and there is no separate colour hook to override.
 */
public class ColoredButton extends Button.Plain {
	private int fgColor = 0xFFFFFF;

	public ColoredButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
	}

	public void setFGColor(int fgColor) {
		this.fgColor = fgColor;
	}

	@Override
	public Component getMessage() {
		//inactive keeps vanilla's own greyed-out message instead of fgColor, matching upstream's disabled colour
		return active ? super.getMessage().copy().withStyle(Style.EMPTY.withColor(fgColor)) : super.getMessage();
	}
}
