package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.network.SetPasscodePayload;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

/** The passcode-setting screen (owner sets/replaces the code). 1:1 with upstream SetPasscodeScreen. */
public class SetPasscodeScreen extends Screen {
	private static final Identifier TEXTURE = SCContent.id("textures/gui/container/blank.png");
	private final int imageWidth = 176;
	private final int imageHeight = 166;
	private final BlockPos pos;
	private final Component setup;
	private final MutableComponent combined;
	private int leftPos;
	private int topPos;
	private EditBox keycodeTextbox;
	private Button saveAndContinueButton;

	public SetPasscodeScreen(BlockPos pos, Component title) {
		super(title);
		this.pos = pos;
		setup = Utils.localize("gui.securitycraft:passcode.setup");
		combined = title.plainCopy().append(Component.literal(" ")).append(setup);
	}

	@Override
	protected void init() {
		super.init();
		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;
		saveAndContinueButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:passcode.save"), this::saveAndContinueButtonClicked).bounds(width / 2 - 48, height / 2 + 30 + 10, 100, 20).build());
		saveAndContinueButton.active = false;
		keycodeTextbox = addRenderableWidget(new EditBox(font, width / 2 - 37, height / 2 - 47, 77, 12, Component.empty()));
		keycodeTextbox.setMaxLength(Integer.MAX_VALUE);
		// 26.x removed EditBox#setFilter; keep the passcode digit-only (so it stays enterable on the number-pad
		// CheckPasscodeScreen) by sanitizing the value in the responder — covers both typing and paste.
		keycodeTextbox.setResponder(text -> {
			String digits = text.replaceAll("\\D", "");

			if (!digits.equals(text))
				keycodeTextbox.setValue(digits);
			else
				saveAndContinueButton.active = !text.isEmpty();
		});
		setInitialFocus(keycodeTextbox);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(extractor, mouseX, mouseY, partialTicks);
		extractor.text(font, "CODE:", width / 2 - 67, height / 2 - 47 + 2, 0xFF404040, false);

		if (font.width(combined) < imageWidth - 10)
			extractor.text(font, combined, width / 2 - font.width(combined) / 2, topPos + 6, 0xFF404040, false);
		else {
			extractor.text(font, title, width / 2 - font.width(title) / 2, topPos + 6, 0xFF404040, false);
			extractor.text(font, setup, width / 2 - font.width(setup) / 2, topPos + 16, 0xFF404040, false);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(extractor, mouseX, mouseY, partialTick);
		extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}
		else if (event.key() == InputConstants.KEY_NUMPADENTER || event.key() == InputConstants.KEY_RETURN && saveAndContinueButton.active)
			saveAndContinueButtonClicked(saveAndContinueButton);

		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void saveAndContinueButtonClicked(Button button) {
		ClientPlayNetworking.send(new SetPasscodePayload(pos, keycodeTextbox.getValue()));
		onClose();
	}
}
