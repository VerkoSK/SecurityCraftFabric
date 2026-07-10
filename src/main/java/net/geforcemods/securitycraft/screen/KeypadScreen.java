package net.geforcemods.securitycraft.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.network.CheckPasscodePayload;
import net.geforcemods.securitycraft.network.SetPasscodePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Numeric passcode screen. In setup mode the value becomes the keypad's new code; otherwise it is
 * sent as an attempt. Kept deliberately simple (a single field) compared to the upstream 10-button
 * keypad UI, which is on the roadmap.
 */
public class KeypadScreen extends Screen {
	private static final int MAX_LENGTH = 8;
	private final BlockPos pos;
	private final boolean setup;
	private final String ownerName;
	private EditBox input;

	public KeypadScreen(BlockPos pos, boolean setup, String ownerName) {
		super(Component.translatable(setup ? "gui.securitycraft:keypad.setup" : "gui.securitycraft:keypad.enter"));
		this.pos = pos;
		this.setup = setup;
		this.ownerName = ownerName;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = height / 2 - 10;

		input = new EditBox(font, centerX - 60, y, 120, 20, Component.translatable("gui.securitycraft:keypad.field"));
		input.setMaxLength(MAX_LENGTH);
		input.setFilter(s -> s.matches("[0-9]*"));
		addRenderableWidget(input);
		setInitialFocus(input);

		addRenderableWidget(Button.builder(Component.translatable(setup ? "gui.securitycraft:keypad.save" : "gui.securitycraft:keypad.submit"), b -> submit()).bounds(centerX - 60, y + 26, 58, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(centerX + 2, y + 26, 58, 20).build());
	}

	private void submit() {
		String code = input.getValue();

		if (code == null || code.isBlank())
			return;

		if (setup)
			ClientPlayNetworking.send(new SetPasscodePayload(pos, code));
		else
			ClientPlayNetworking.send(new CheckPasscodePayload(pos, code));

		onClose();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(font, title, width / 2, height / 2 - 40, 0xFFFFFF);
		guiGraphics.drawCenteredString(font, Component.translatable("gui.securitycraft:keypad.owner", ownerName), width / 2, height / 2 - 28, 0xA0A0A0);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
