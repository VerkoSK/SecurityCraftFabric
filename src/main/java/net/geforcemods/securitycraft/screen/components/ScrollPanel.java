package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

/**
 * Fabric equivalent of Forge's {@code net.minecraftforge.client.gui.widget.ScrollPanel}, preserving the exact
 * protected fields and method signatures expected by SecurityCraft's scrollable list widgets.
 */
public abstract class ScrollPanel implements Renderable, GuiEventListener, NarratableEntry {
	protected final Minecraft client;
	protected final int width;
	protected final int height;
	protected final int top;
	protected final int bottom;
	protected final int right;
	protected final int left;
	protected boolean scrolling;
	protected float scrollDistance;
	protected boolean captureMouse = true;
	protected final int border;
	protected final int barWidth;
	protected final int barLeft;
	protected final int bgColorFrom;
	protected final int bgColorTo;
	protected final int barBgColor;
	protected final int barColor;
	protected final int barBorderColor;
	private boolean focused;

	public ScrollPanel(Minecraft client, int width, int height, int top, int left) {
		this(client, width, height, top, left, 4);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border) {
		this(client, width, height, top, left, border, 6);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth) {
		this(client, width, height, top, left, border, barWidth, 0xC0101010, 0xD0101010);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColor) {
		this(client, width, height, top, left, border, barWidth, bgColor, bgColor);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColorFrom, int bgColorTo) {
		this(client, width, height, top, left, border, barWidth, bgColorFrom, bgColorTo, 0xFF000000, 0xFF808080, 0xFFC0C0C0);
	}

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColorFrom, int bgColorTo, int barBgColor, int barColor, int barBorderColor) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
		this.bottom = height + this.top;
		this.right = width + this.left;
		this.barLeft = this.left + this.width - barWidth;
		this.border = border;
		this.barWidth = barWidth;
		this.bgColorFrom = bgColorFrom;
		this.bgColorTo = bgColorTo;
		this.barBgColor = barBgColor;
		this.barColor = barColor;
		this.barBorderColor = barBorderColor;
	}

	protected abstract int getContentHeight();

	protected void drawBackground(GuiGraphicsExtractor extractor, float partialTick) {
		if (bgColorFrom != 0 || bgColorTo != 0)
			extractor.fillGradient(left, top, right, bottom, bgColorFrom, bgColorTo);
	}

	protected abstract void drawPanel(GuiGraphicsExtractor extractor, int entryRight, int relativeY, int mouseX, int mouseY);

	protected boolean clickPanel(double mouseX, double mouseY, int button) {
		return false;
	}

	private int getMaxScroll() {
		return getContentHeight() - (height - border);
	}

	private void applyScrollLimits() {
		int max = getMaxScroll();

		if (max < 0)
			max /= 2;

		if (scrollDistance < 0.0F)
			scrollDistance = 0.0F;

		if (scrollDistance > max)
			scrollDistance = max;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			scrollDistance += (float) (-scrollY * getScrollAmount());
			applyScrollLimits();
			return true;
		}

		return false;
	}

	protected int getScrollAmount() {
		return 20;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= bottom;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (superMouseClicked(event.x(), event.y(), event.button()))
			return true;

		scrolling = event.button() == 0 && event.x() >= barLeft && event.x() < barLeft + barWidth && event.y() >= top && event.y() <= bottom;

		if (scrolling)
			return true;

		int mouseListY = ((int) event.y()) - top - getContentHeight() + (int) scrollDistance - border;

		if (event.x() >= left && event.x() <= right && mouseListY < 0)
			return clickPanel(event.x() - left, event.y() - top + (int) scrollDistance - border, event.button());

		return false;
	}

	private boolean superMouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (superMouseReleased(event.x(), event.y(), event.button()))
			return true;

		boolean wasScrolling = scrolling;

		scrolling = false;
		return wasScrolling;
	}

	private boolean superMouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	private int getBarHeight() {
		int barHeight = (height * height) / getContentHeight();

		if (barHeight < 32)
			barHeight = 32;

		if (barHeight > height - border * 2)
			barHeight = height - border * 2;

		return barHeight;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (scrolling) {
			int maxScroll = height - getBarHeight();
			double moved = deltaY / maxScroll;

			scrollDistance += (float) (getMaxScroll() * moved);
			applyScrollLimits();
			return true;
		}

		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		drawBackground(extractor, partialTick);
		extractor.enableScissor(left, top, right, bottom);

		int baseY = top + border - (int) scrollDistance;

		drawPanel(extractor, right, baseY, mouseX, mouseY);

		int extraHeight = (getContentHeight() + border) - height;

		if (extraHeight > 0) {
			int barHeight = getBarHeight();
			int barTop = (int) scrollDistance * (height - barHeight) / extraHeight + top;

			if (barTop < top)
				barTop = top;

			extractor.fill(barLeft, top, barLeft + barWidth, bottom, barBgColor);
			extractor.fill(barLeft, barTop, barLeft + barWidth, barTop + barHeight, barBorderColor);
			extractor.fill(barLeft, barTop, barLeft + barWidth - 1, barTop + barHeight - 1, barColor);
		}

		extractor.disableScissor();
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return focused;
	}
}
