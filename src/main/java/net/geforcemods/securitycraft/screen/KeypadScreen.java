package net.geforcemods.securitycraft.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.network.CheckPasscodePayload;
import net.geforcemods.securitycraft.network.SetPasscodePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Numeric passcode screen. In setup mode the value becomes the keypad's new code; otherwise it is
 * sent as an attempt.
 */
public class KeypadScreen extends Screen {
	private static final int MAX_LENGTH = 8;
	private final BlockPos pos;
	private final boolean setup;
	private final String ownerName;
	private TextFieldWidget input;

	public KeypadScreen(BlockPos pos, boolean setup, String ownerName) {
		super(Text.translatable(setup ? "gui.securitycraft:keypad.setup" : "gui.securitycraft:keypad.enter"));
		this.pos = pos;
		this.setup = setup;
		this.ownerName = ownerName;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = height / 2 - 10;

		input = new TextFieldWidget(textRenderer, centerX - 60, y, 120, 20, Text.translatable("gui.securitycraft:keypad.field"));
		input.setMaxLength(MAX_LENGTH);
		input.setTextPredicate(s -> s.matches("[0-9]*"));
		addDrawableChild(input);
		setInitialFocus(input);

		addDrawableChild(ButtonWidget.builder(Text.translatable(setup ? "gui.securitycraft:keypad.save" : "gui.securitycraft:keypad.submit"), b -> submit()).dimensions(centerX - 60, y + 26, 58, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> close()).dimensions(centerX + 2, y + 26, 58, 20).build());
	}

	private void submit() {
		String code = input.getText();

		if (code == null || code.isBlank())
			return;

		if (setup)
			ClientPlayNetworking.send(new SetPasscodePayload(pos, code));
		else
			ClientPlayNetworking.send(new CheckPasscodePayload(pos, code));

		close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 40, 0xFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.securitycraft:keypad.owner", ownerName), width / 2, height / 2 - 28, 0xA0A0A0);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
