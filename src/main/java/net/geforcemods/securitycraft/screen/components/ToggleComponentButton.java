package net.geforcemods.securitycraft.screen.components;

import java.util.function.IntFunction;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** A button that cycles through a fixed number of states, updating its label from an index->Component function. */
public class ToggleComponentButton extends Button implements IToggleableButton {
	private final IntFunction<Component> onValueChange;
	private final int toggleCount;
	private int currentIndex;

	public ToggleComponentButton(int xPos, int yPos, int width, int height, IntFunction<Component> onValueChange, int initialValue, int toggleCount, OnPress onPress) {
		super(xPos, yPos, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.onValueChange = onValueChange;
		this.currentIndex = initialValue;
		this.toggleCount = toggleCount;
		onValueChange();
	}

	@Override
	public void onPress(InputWithModifiers input) {
		cycleIndex(input.hasShiftDown() ? -1 : 1);
		super.onPress(input);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		extractDefaultSprite(extractor);
		extractDefaultLabel(extractor.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (isMouseOver(mouseX, mouseY)) {
			cycleIndex(-(int) Math.signum(scrollY));
			onPress(new KeyEvent(0, 0, shiftModifier()));
			return true;
		}

		return false;
	}

	private static int shiftModifier() {
		Window window = Minecraft.getInstance().getWindow();

		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT) ? GLFW.GLFW_MOD_SHIFT : 0;
	}

	public void cycleIndex(int value) {
		setCurrentIndex(Math.floorMod(currentIndex + value, toggleCount));
		onValueChange();
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void setCurrentIndex(int newIndex) {
		currentIndex = Math.floorMod(newIndex, toggleCount);
	}

	public void onValueChange() {
		setMessage(onValueChange.apply(currentIndex));
	}
}
