package net.geforcemods.securitycraft.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.GameRenderer;

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

	protected void drawBackground(GuiGraphics guiGraphics, Tesselator tess, float partialTick) {
		if (bgColorFrom != 0 || bgColorTo != 0)
			guiGraphics.fillGradient(left, top, right, bottom, bgColorFrom, bgColorTo);
	}

	protected abstract void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY);

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
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (scroll != 0) {
			scrollDistance += (float) (-scroll * getScrollAmount());
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (superMouseClicked(mouseX, mouseY, button))
			return true;

		scrolling = button == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth && mouseY >= top && mouseY <= bottom;

		if (scrolling)
			return true;

		int mouseListY = ((int) mouseY) - top - getContentHeight() + (int) scrollDistance - border;

		if (mouseX >= left && mouseX <= right && mouseListY < 0)
			return clickPanel(mouseX - left, mouseY - top + (int) scrollDistance - border, button);

		return false;
	}

	private boolean superMouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (scrolling) {
			scrolling = false;
			return true;
		}

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
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Tesselator tess = Tesselator.getInstance();

		drawBackground(guiGraphics, tess, partialTick);
		guiGraphics.enableScissor(left, top, right, bottom);

		int baseY = top + border - (int) scrollDistance;

		drawPanel(guiGraphics, right, baseY, tess, mouseX, mouseY);
		RenderSystem.disableDepthTest();

		int extraHeight = (getContentHeight() + border) - height;

		if (extraHeight > 0) {
			int barHeight = getBarHeight();
			int barTop = (int) scrollDistance * (height - barHeight) / extraHeight + top;

			if (barTop < top)
				barTop = top;

			int barBgAlpha = barBgColor >> 24 & 0xff;
			int barBgRed = barBgColor >> 16 & 0xff;
			int barBgGreen = barBgColor >> 8 & 0xff;
			int barBgBlue = barBgColor & 0xff;

			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			BufferBuilder worldr = tess.getBuilder();
			worldr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			worldr.vertex(barLeft, bottom, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
			worldr.vertex(barLeft + barWidth, bottom, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
			worldr.vertex(barLeft + barWidth, top, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();
			worldr.vertex(barLeft, top, 0.0D).color(barBgRed, barBgGreen, barBgBlue, barBgAlpha).endVertex();

			int barBorderAlpha = barBorderColor >> 24 & 0xff;
			int barBorderRed = barBorderColor >> 16 & 0xff;
			int barBorderGreen = barBorderColor >> 8 & 0xff;
			int barBorderBlue = barBorderColor & 0xff;

			worldr.vertex(barLeft, barTop + barHeight, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
			worldr.vertex(barLeft + barWidth, barTop + barHeight, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
			worldr.vertex(barLeft + barWidth, barTop, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();
			worldr.vertex(barLeft, barTop, 0.0D).color(barBorderRed, barBorderGreen, barBorderBlue, barBorderAlpha).endVertex();

			int barAlpha = barColor >> 24 & 0xff;
			int barRed = barColor >> 16 & 0xff;
			int barGreen = barColor >> 8 & 0xff;
			int barBlue = barColor & 0xff;

			worldr.vertex(barLeft, barTop + barHeight - 1, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
			worldr.vertex(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
			worldr.vertex(barLeft + barWidth - 1, barTop, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
			worldr.vertex(barLeft, barTop, 0.0D).color(barRed, barGreen, barBlue, barAlpha).endVertex();
			tess.end();
		}

		guiGraphics.disableScissor();
		RenderSystem.enableDepthTest();
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
