package net.geforcemods.securitycraft.screen.components;

import java.util.function.IntFunction;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
	public void onPress() {
		cycleIndex(Screen.hasShiftDown() ? -1 : 1);
		super.onPress();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (isMouseOver(mouseX, mouseY)) {
			cycleIndex(-(int) Math.signum(scrollY));
			onPress();
			return true;
		}

		return false;
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
