package net.geforcemods.securitycraft.screen;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.CheckPasscodePayload;
import net.geforcemods.securitycraft.screen.components.CallbackCheckbox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The passcode-entry screen: number pad, censored input with a show-passcode toggle, and cooldown UI. 1:1 with upstream CheckPasscodeScreen. */
public class CheckPasscodeScreen extends Screen {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/check_passcode.png");
	private static final Component COOLDOWN_TEXT_1 = Component.translatable("gui.securitycraft:passcode.cooldown1");
	private final char[] allowedChars = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\b', '\u001B'
	}; //0-9, backspace and escape
	private final int imageWidth = 176;
	private final int imageHeight = 186;
	private final BlockPos pos;
	private int cooldownText1XPos;
	private int leftPos;
	private int topPos;
	private CensoringEditBox keycodeTextbox;
	private boolean wasOnCooldownLastRenderTick = false;

	public CheckPasscodeScreen(BlockPos pos, Component title) {
		super(title);
		this.pos = pos;
	}

	@Override
	protected void init() {
		super.init();
		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;
		cooldownText1XPos = width / 2 - font.width(COOLDOWN_TEXT_1) / 2;
		addRenderableWidget(new CallbackCheckbox(width / 2 - 37, height / 2 - 55, 12, 12, Component.translatable("gui.securitycraft:passcode.showPasscode"), false, newState -> keycodeTextbox.setCensoring(!newState), 0xFF404040));
		addNumberButton("1", width / 2 - 33, height / 2 - 35, b -> addNumberToString(1));
		addNumberButton("2", width / 2 - 8, height / 2 - 35, b -> addNumberToString(2));
		addNumberButton("3", width / 2 + 17, height / 2 - 35, b -> addNumberToString(3));
		addNumberButton("4", width / 2 - 33, height / 2 - 10, b -> addNumberToString(4));
		addNumberButton("5", width / 2 - 8, height / 2 - 10, b -> addNumberToString(5));
		addNumberButton("6", width / 2 + 17, height / 2 - 10, b -> addNumberToString(6));
		addNumberButton("7", width / 2 - 33, height / 2 + 15, b -> addNumberToString(7));
		addNumberButton("8", width / 2 - 8, height / 2 + 15, b -> addNumberToString(8));
		addNumberButton("9", width / 2 + 17, height / 2 + 15, b -> addNumberToString(9));
		addNumberButton("←", width / 2 - 33, height / 2 + 40, b -> removeLastCharacter());
		addNumberButton("0", width / 2 - 8, height / 2 + 40, b -> addNumberToString(0));
		addNumberButton("✔", width / 2 + 17, height / 2 + 40, b -> checkCode(keycodeTextbox.getValue()));
		keycodeTextbox = addRenderableWidget(new CensoringEditBox(font, width / 2 - 37, height / 2 - 72, 77, 12, Component.empty()) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return active && super.mouseClicked(mouseX, mouseY, button);
			}

			@Override
			public boolean canConsumeInput() {
				return active && isVisible();
			}
		});
		keycodeTextbox.setMaxLength(Integer.MAX_VALUE);
		keycodeTextbox.setFilter(s -> s.matches("\\d*\\**")); //allow any amount of digits and any amount of asterisks

		if (isOnCooldown())
			toggleChildrenActive(false);
		else
			setInitialFocus(keycodeTextbox);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawString(font, title, width / 2 - font.width(title) / 2, topPos + 6, 0xFF404040, false);

		if (isOnCooldown()) {
			long secondsLeft = Math.max(getCooldownEnd() - System.currentTimeMillis(), 0) / 1000 + 1; //+1 so that the text doesn't say "0 seconds left" for a whole second
			Component text = Component.translatable("gui.securitycraft:passcode.cooldown2", secondsLeft);

			guiGraphics.drawString(font, COOLDOWN_TEXT_1, cooldownText1XPos, height / 2 + 65, 0xFF404040, false);
			guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, height / 2 + 75, 0xFF404040, false);
			wasOnCooldownLastRenderTick = true;
		}
		else if (wasOnCooldownLastRenderTick) {
			wasOnCooldownLastRenderTick = false;
			toggleChildrenActive(true);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(guiGraphics);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !keycodeTextbox.getValue().isEmpty())
			minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15F, 1.0F);

		if (!super.keyPressed(keyCode, scanCode, modifiers) && !keycodeTextbox.keyPressed(keyCode, scanCode, modifiers)) {
			if (minecraft.options.keyInventory.matches(keyCode, scanCode))
				onClose();

			if (!isOnCooldown() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
				minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15F, 1.0F);
				checkCode(keycodeTextbox.getValue());
			}
		}

		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		if (!isOnCooldown() && isValidChar(typedChar)) {
			keycodeTextbox.charTyped(typedChar, keyCode);
			minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15F, 1.0F);
		}

		return true;
	}

	private boolean isValidChar(char c) {
		for (char allowedChar : allowedChars) {
			if (c == allowedChar)
				return true;
		}

		return false;
	}

	private void addNumberButton(String label, int x, int y, Button.OnPress onPress) {
		addRenderableWidget(Button.builder(Component.literal(label), onPress).bounds(x, y, 20, 20).build());
	}

	private void addNumberToString(int number) {
		keycodeTextbox.insertText("" + number);
	}

	private void removeLastCharacter() {
		if (!keycodeTextbox.getValue().isEmpty())
			keycodeTextbox.deleteChars(-1);
	}

	private void toggleChildrenActive(boolean setActive) {
		children().forEach(listener -> {
			if (listener instanceof AbstractWidget widget)
				widget.active = setActive;
		});
		keycodeTextbox.setFocused(setActive);
	}

	public void checkCode(String code) {
		if (hasSmartModule())
			toggleChildrenActive(false);

		keycodeTextbox.setValue("");
		ClientPlayNetworking.send(new CheckPasscodePayload(pos, code));
	}

	private BlockEntity blockEntity() {
		return minecraft != null && minecraft.level != null ? minecraft.level.getBlockEntity(pos) : null;
	}

	private boolean isOnCooldown() {
		return blockEntity() instanceof PasscodeProtected pp && pp.isOnCooldown();
	}

	private long getCooldownEnd() {
		return blockEntity() instanceof PasscodeProtected pp ? pp.getCooldownEnd() : 0;
	}

	private boolean hasSmartModule() {
		return blockEntity() instanceof IModuleInventory inv && inv.isModuleEnabled(ModuleType.SMART);
	}

	/** An {@link EditBox} that renders its contents as asterisks unless censoring is toggled off. */
	public static class CensoringEditBox extends EditBox {
		private String renderedText = "";
		private boolean shouldCensor = true;

		public CensoringEditBox(Font font, int x, int y, int width, int height, Component message) {
			super(font, x, y, width, height, message);
			setResponder(this::updateRenderedText);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			String originalValue = value;
			boolean success;

			value = renderedText;
			success = super.mouseClicked(mouseX, mouseY, button);
			value = originalValue;
			return success;
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			String originalValue = value;

			value = renderedText;
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
			value = originalValue;
		}

		@Override
		public void scrollTo(int position) {
			String originalValue = value;

			updateRenderedText(originalValue);
			value = renderedText;
			super.scrollTo(position);
			value = originalValue;
		}

		public void setCensoring(boolean shouldCensor) {
			this.shouldCensor = shouldCensor;
			updateRenderedText(value);
		}

		private void updateRenderedText(String original) {
			if (shouldCensor) {
				String x = "";

				for (int i = 1; i <= original.length(); i++) {
					x += "*";
				}

				renderedText = x;
			}
			else
				renderedText = original;
		}
	}
}
