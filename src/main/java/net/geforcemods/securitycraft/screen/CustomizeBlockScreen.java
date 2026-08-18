package net.geforcemods.securitycraft.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.inventory.CustomizeBlockMenu;
import net.geforcemods.securitycraft.network.SetOptionPayload;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The Universal Block Modifier's screen. The module slots sit inside the panel; the options are laid out in a
 * column to its right, the way the original does it, so a block with many options does not overflow the panel.
 */
public class CustomizeBlockScreen extends AbstractContainerScreen<CustomizeBlockMenu> {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/blank.png");
	/** Every option this port carries is one of the shared ones, so they all live under the generic namespace. */
	private static final String DENOTATION = "generic";
	private final Option<?>[] options;
	private final BlockPos pos;

	public CustomizeBlockScreen(CustomizeBlockMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		imageWidth = 176;
		imageHeight = 186;
		inventoryLabelY = imageHeight - 94;
		options = menu.be instanceof ICustomizable customizable ? customizable.customOptions() : new Option<?>[0];
		pos = menu.be != null ? menu.be.getBlockPos() : BlockPos.ZERO;
	}

	@Override
	protected void init() {
		super.init();

		for (int i = 0; i < options.length; i++) {
			Option<?> option = options[i];
			int index = i;
			int x = leftPos + imageWidth + 4;
			int y = topPos + 6 + i * 22;

			if (option instanceof Option.IntOption || option instanceof Option.DoubleOption)
				addRenderableWidget(new OptionSlider(x, y, option, index)).setTooltip(tooltip(option));
			else {
				Button button = addRenderableWidget(Button.builder(label(option), b -> {
					ClientPlayNetworking.send(new SetOptionPayload(pos, index, true, 0.0));
					option.toggle();
					b.setMessage(label(option));
					b.setTooltip(tooltip(option));
				}).bounds(x, y, 130, 20).build());

				button.setTooltip(tooltip(option));
			}
		}
	}

	/** The translation already contains the placeholder for the value, so it is passed in as an argument. */
	private Component label(Option<?> option) {
		return Utils.localize(option.getKey(DENOTATION), option.getValueText());
	}

	private Tooltip tooltip(Option<?> option) {
		return Tooltip.create(Component.translatable(option.getDescriptionKey(DENOTATION)));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		//the blank background has no slot artwork, so the module slots are outlined here
		for (int i = 0; i < menu.slots.size(); i++) {
			var slot = menu.slots.get(i);

			if (i < menu.moduleSlotCount())
				guiGraphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF8B8B8B);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 5, 0xFF404040, false);
		guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
	}

	/** Numeric options are dragged rather than clicked, so the value can be moved across its whole range. */
	private class OptionSlider extends AbstractSliderButton {
		private final Option<?> option;
		private final int index;
		private final double min;
		private final double max;

		OptionSlider(int x, int y, Option<?> option, int index) {
			super(x, y, 130, 20, Component.empty(), 0.0);
			this.option = option;
			this.index = index;
			min = ((Number) option.getMin()).doubleValue();
			max = ((Number) option.getMax()).doubleValue();
			value = max > min ? (((Number) option.get()).doubleValue() - min) / (max - min) : 0.0;
			updateMessage();
		}

		private double actual() {
			return min + value * (max - min);
		}

		@Override
		protected void updateMessage() {
			double actual = actual();
			Object shown = option instanceof Option.IntOption ? (int) Math.round(actual) : String.format("%.1f", actual);

			setMessage(Utils.localize(option.getKey(DENOTATION), shown));
		}

		@Override
		protected void applyValue() {
			ClientPlayNetworking.send(new SetOptionPayload(pos, index, false, actual()));
		}
	}
}
