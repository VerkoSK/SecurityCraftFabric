package net.geforcemods.securitycraft.screen.components;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * A numeric slider that reports its new value to a callback on release. Fabric stand-in for upstream's
 * {@code CallbackSlider}, which extends NeoForge's {@code ForgeSlider}; vanilla has no min/max/step slider, so
 * this reimplements just the pieces {@link net.geforcemods.securitycraft.screen.CustomizeBlockScreen} and other screens need.
 */
public class CallbackSlider extends AbstractSliderButton {
	private final Component prefix;
	private final Component suffix;
	private double min;
	private double max;
	private final double step;
	private final DecimalFormat format;
	private final boolean drawString;
	private final Consumer<CallbackSlider> callback;

	public CallbackSlider(int x, int y, int width, int height, double min, double max, double current, double step, Consumer<CallbackSlider> callback) {
		this(x, y, width, height, Component.empty(), Component.empty(), min, max, current, step, 0, false, callback);
	}

	public CallbackSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double current, boolean drawString, Consumer<CallbackSlider> callback) {
		this(x, y, width, height, prefix, suffix, min, max, current, 1.0D, 0, drawString, callback);
	}

	public CallbackSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double current, double step, int precision, boolean drawString, Consumer<CallbackSlider> callback) {
		super(x, y, width, height, Component.empty(), max > min ? Mth.clamp((current - min) / (max - min), 0.0, 1.0) : 0.0);
		this.prefix = prefix;
		this.suffix = suffix;
		this.min = min;
		this.max = max;
		this.step = step;
		this.drawString = drawString;
		this.callback = callback;

		String pattern = "0";

		if (precision > 0) {
			pattern += ".";
			pattern += "0".repeat(precision);
		}

		this.format = new DecimalFormat(pattern);
		updateMessage();
	}

	public double getMinValue() {
		return min;
	}

	public void setMinValue(double min) {
		this.min = min;
	}

	public double getMaxValue() {
		return max;
	}

	public void setMaxValue(double max) {
		this.max = max;
	}

	public void setFGColor(int color) {}

	public double getValue() {
		double actual = min + value * (max - min);

		if (step > 0.0)
			actual = Math.round(actual / step) * step;

		return Mth.clamp(actual, min, max);
	}

	public int getValueInt() {
		return (int) Math.round(getValue());
	}

	public void setActualValue(double newValue) {
		double clamped = Mth.clamp(newValue, min, max);

		if (step > 0.0)
			clamped = Math.round(clamped / step) * step;

		value = max > min ? Mth.clamp((clamped - min) / (max - min), 0.0, 1.0) : 0.0;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		if (drawString)
			setMessage(Component.literal("").append(prefix).append(format.format(getValue())).append(suffix));
		else
			setMessage(Component.empty());
	}

	@Override
	protected void applyValue() {
		updateMessage();

		if (callback != null)
			callback.accept(this);
	}

	@Override
	public void onRelease(net.minecraft.client.input.MouseButtonEvent event) {
		super.onRelease(event);

		if (callback != null)
			callback.accept(this);
	}
}
