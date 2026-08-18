package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * A numeric slider that reports its new value to a callback on release. Fabric stand-in for upstream's
 * {@code CallbackSlider}, which extends NeoForge's {@code ForgeSlider}; vanilla has no min/max/step slider, so
 * this reimplements just the pieces {@link net.geforcemods.securitycraft.screen.CustomizeBlockScreen} needs.
 */
public class CallbackSlider extends AbstractSliderButton {
	private final double min;
	private final double max;
	private final double step;
	private final Consumer<CallbackSlider> callback;

	public CallbackSlider(int x, int y, int width, int height, double min, double max, double current, double step, Consumer<CallbackSlider> callback) {
		super(x, y, width, height, Component.empty(), max > min ? (current - min) / (max - min) : 0.0);
		this.min = min;
		this.max = max;
		this.step = step;
		this.callback = callback;
		updateMessage();
	}

	public double getValue() {
		double actual = min + value * (max - min);

		if (step > 0.0)
			actual = Math.round(actual / step) * step;

		return Math.max(min, Math.min(max, actual));
	}

	public int getValueInt() {
		return (int) Math.round(getValue());
	}

	@Override
	protected void updateMessage() {}

	@Override
	protected void applyValue() {
		if (callback != null)
			callback.accept(this);
	}
}
