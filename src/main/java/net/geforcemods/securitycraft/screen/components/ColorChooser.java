package net.geforcemods.securitycraft.screen.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** HSB colour picker shown as a pushed GUI layer next to the reinforcer screen. */
public class ColorChooser extends Screen {
	private static final Identifier TEXTURE = SCContent.id("textures/gui/container/color_chooser.png");
	private static final Identifier HUE_SLIDER_SPRITE = SCContent.id("widget/color_chooser/hue_slider");
	private static final Identifier HUE_SLIDER_HIGHLIGHTED_SPRITE = SCContent.id("widget/color_chooser/hue_slider_highlighted");
	private static final Identifier FIELD_SELECTOR_SPRITE = SCContent.id("widget/color_chooser/field_selector");
	private static final Identifier FIELD_SELECTOR_HIGHLIGHTED_SPRITE = SCContent.id("widget/color_chooser/field_selector_highlighted");
	private static final int COLOR_FIELD_SIZE = 75;
	public boolean disabled = true;
	private final int xStart, yStart;
	private final List<Rect2i> extraAreas = new ArrayList<>();
	private final Component rText = Component.literal("R");
	private final Component gText = Component.literal("G");
	private final Component bText = Component.literal("B");
	private final Component rgbHexText = Component.literal("#");
	private boolean clickedInDragRegion = false;
	private boolean updating = false;
	private float h, s, b;
	private final int colorFieldTop, colorFieldBottom, colorFieldLeft, colorFieldRight;
	private final HoverChecker colorFieldHoverChecker;
	private float selectionX, selectionY;
	private final Consumer<Integer> onColorChange;
	private int rgbColor;
	private EditBox rBox, gBox, bBox, rgbHexBox;
	private HueSlider hueSlider;

	public ColorChooser(Component title, int xStart, int yStart, int rgbColor, Consumer<Integer> onColorChange) {
		super(title);
		this.xStart = xStart;
		this.yStart = yStart;
		colorFieldLeft = xStart + 6;
		colorFieldTop = yStart + 6;
		colorFieldRight = colorFieldLeft + COLOR_FIELD_SIZE;
		colorFieldBottom = colorFieldTop + COLOR_FIELD_SIZE;
		this.rgbColor = rgbColor & 0xFFFFFF;
		this.onColorChange = onColorChange;
		updateHSBValues(this.rgbColor >> 16 & 255, this.rgbColor >> 8 & 255, this.rgbColor & 255);
		colorFieldHoverChecker = new HoverChecker(colorFieldTop, colorFieldBottom, colorFieldLeft, colorFieldRight);
	}

	@Override
	protected void init() {
		int red = rgbColor >> 16 & 255;
		int green = rgbColor >> 8 & 255;
		int blue = rgbColor & 255;

		updateHSBValues(red, green, blue);
		extraAreas.clear();
		extraAreas.add(new Rect2i(xStart, yStart, 144, 108));
		hueSlider = addRenderableWidget(new HueSlider(colorFieldLeft - 2, yStart + 85, 82, 20, h * 360.0D));
		rBox = addRenderableWidget(new EditBox(font, colorFieldRight + 13, colorFieldTop, 26, 10, rText));
		gBox = addRenderableWidget(new EditBox(font, colorFieldRight + 13, colorFieldTop + 15, 26, 10, gText));
		bBox = addRenderableWidget(new EditBox(font, colorFieldRight + 13, colorFieldTop + 30, 26, 10, bText));
		rgbHexBox = addRenderableWidget(new EditBox(font, colorFieldRight + 13, colorFieldTop + 45, 46, 10, rgbHexText));
		rBox.setMaxLength(3);
		gBox.setMaxLength(3);
		bBox.setMaxLength(3);
		rgbHexBox.setMaxLength(6);
		rBox.setFilter(this::isByteString);
		gBox.setFilter(this::isByteString);
		bBox.setFilter(this::isByteString);
		rgbHexBox.setFilter(string -> string.matches("[0-9a-fA-F]*"));
		rBox.setValue("" + red);
		gBox.setValue("" + green);
		bBox.setValue("" + blue);
		rgbHexBox.setValue(Integer.toHexString(rgbColor));
		rBox.setResponder(makeRgbResponder(rBox));
		gBox.setResponder(makeRgbResponder(gBox));
		bBox.setResponder(makeRgbResponder(bBox));
		rgbHexBox.setResponder(string -> {
			if (updating || string.isEmpty())
				return;

			int hexColor = Integer.parseInt(string, 16);

			updateHSBValues(hexColor >> 16 & 255, hexColor >> 8 & 255, hexColor & 255);
			updateTextFields(rgbHexBox);
			onColorChange();
		});
	}

	private boolean isByteString(String string) {
		return string.isEmpty() || StringUtils.isNumeric(string);
	}

	private Consumer<String> makeRgbResponder(EditBox box) {
		return string -> {
			if (updating || string.isEmpty())
				return;

			int number = Integer.parseInt(string);

			if (number < 0)
				box.setValue("0");
			else if (number > 255)
				box.setValue("255");

			updateHSBValues(parseBox(rBox), parseBox(gBox), parseBox(bBox));
			updateTextFields(box);
			onColorChange();
		};
	}

	private int parseBox(EditBox box) {
		return box.getValue().isEmpty() ? 0 : Integer.parseInt(box.getValue());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, colorFieldHoverChecker.checkHover(mouseX, mouseY) ? FIELD_SELECTOR_HIGHLIGHTED_SPRITE : FIELD_SELECTOR_SPRITE, (int) selectionX - 1, (int) selectionY - 1, 3, 3);
		guiGraphics.drawString(font, rText, colorFieldRight + 5, colorFieldTop + 1, 0xFF404040, false);
		guiGraphics.drawString(font, gText, colorFieldRight + 5, colorFieldTop + 16, 0xFF404040, false);
		guiGraphics.drawString(font, bText, colorFieldRight + 5, colorFieldTop + 31, 0xFF404040, false);
		guiGraphics.drawString(font, rgbHexText, colorFieldRight + 5, colorFieldTop + 46, 0xFF404040, false);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xStart, yStart, 0, 0, 145, 109, 256, 256);
		ClientUtils.fillHorizontalGradient(guiGraphics, 0, colorFieldLeft, colorFieldTop, colorFieldRight + 1, colorFieldBottom + 1, 0xFFFFFFFF, ClientUtils.HSBtoRGB(h, 1.0F, 1.0F) | 0xFF000000);
		guiGraphics.fillGradient(colorFieldLeft, colorFieldTop, colorFieldRight + 1, colorFieldBottom + 1, 0x00000000, 0xFF000000);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		super.mouseDragged(event, dragX, dragY);

		if (event.button() == 0 && clickedInDragRegion) {
			setSelection(event.x(), event.y());
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		super.mouseClicked(event, doubleClick);
		clickedInDragRegion = colorFieldHoverChecker.checkHover(event.x(), event.y());

		if (clickedInDragRegion)
			setSelection(event.x(), event.y());

		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		super.mouseReleased(event);
		clickedInDragRegion = false;
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		super.onClose();
		disabled = true;
	}

	public List<Rect2i> getGuiExtraAreas() {
		return extraAreas;
	}

	public int getARGBColor() {
		return rgbColor | 0xFF000000;
	}

	public int getRGBColor() {
		return rgbColor;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void setSelection(double mouseX, double mouseY) {
		selectionX = (int) Mth.clamp(mouseX, colorFieldLeft, colorFieldRight);
		selectionY = (int) Mth.clamp(mouseY, colorFieldTop, colorFieldBottom);
		s = (selectionX - colorFieldLeft) / COLOR_FIELD_SIZE;
		b = 1.0F - (selectionY - colorFieldTop) / COLOR_FIELD_SIZE;
		updateTextFields(null);
		onColorChange();
	}

	private void updateHSBValues(int red, int green, int blue) {
		float[] hsbColor = ClientUtils.RGBtoHSB(red, green, blue);

		h = hsbColor[0];
		s = hsbColor[1];
		b = hsbColor[2];
		updateSelection();
	}

	private void updateTextFields(EditBox excluded) {
		int currentRGBColor = ClientUtils.HSBtoRGB(h, s, b);
		int red = currentRGBColor >> 16 & 255;
		int green = currentRGBColor >> 8 & 255;
		int blue = currentRGBColor & 255;

		rgbColor = currentRGBColor;
		updating = true;

		if (excluded != rBox)
			rBox.setValue("" + red);

		if (excluded != gBox)
			gBox.setValue("" + green);

		if (excluded != bBox)
			bBox.setValue("" + blue);

		if (excluded != rgbHexBox)
			rgbHexBox.setValue(Integer.toHexString(currentRGBColor));

		updating = false;
	}

	private void updateSelection() {
		selectionX = s * COLOR_FIELD_SIZE + colorFieldLeft;
		selectionY = -b * COLOR_FIELD_SIZE + COLOR_FIELD_SIZE + colorFieldTop;

		if (hueSlider != null)
			hueSlider.setHue(h * 360.0D);
	}

	public void onColorChange() {
		onColorChange.accept(rgbColor);
	}

	class HueSlider extends AbstractSliderButton {
		public HueSlider(int x, int y, int width, int height, double currentValue) {
			super(x, y, width, height, Component.empty(), currentValue / 360.0D);
		}

		public void setHue(double hue) {
			value = hue / 360.0D;
		}

		@Override
		protected void updateMessage() {}

		@Override
		protected void applyValue() {
			h = (float) value;
			updateTextFields(null);
			onColorChange();
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, isHoveredOrFocused() ? HUE_SLIDER_HIGHLIGHTED_SPRITE : HUE_SLIDER_SPRITE, getX() + (int) (value * (width - 8)), getY(), 6, height);
		}
	}
}
