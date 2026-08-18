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
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The Universal Block Modifier's screen: the block's module slots along the top, and one control per option
 * below — a button for booleans and enums, a slider for numbers.
 */
public class CustomizeBlockScreen extends AbstractContainerScreen<CustomizeBlockMenu> {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/blank.png");
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

		for (int i = 0; i < options.length && i < 4; i++) {
			Option<?> option = options[i];
			int index = i;
			int x = leftPos + 8;
			int y = topPos + 42 + i * 22;

			if (option instanceof Option.IntOption || option instanceof Option.DoubleOption)
				addRenderableWidget(new OptionSlider(x, y, option, index));
			else {
				Button button = addRenderableWidget(Button.builder(label(option), b -> {
					ClientPlayNetworking.send(new SetOptionPayload(pos, index, true, 0.0));
					b.setMessage(label(option));
				}).bounds(x, y, 160, 20).build());

				button.setTooltip(Tooltip.create(Utils.localize(option.getKey(SCContent.id("").getNamespace()) + ".description")));
			}
		}
	}

	private Component label(Option<?> option) {
		return Component.translatable(option.getKey("securitycraft")).append(": ").append(String.valueOf(option.get()));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
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
			super(x, y, 160, 20, Component.empty(), 0.0);
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
			String shown = option instanceof Option.IntOption ? String.valueOf((int) Math.round(actual)) : String.format("%.1f", actual);

			setMessage(Component.translatable(option.getKey("securitycraft")).append(": ").append(shown));
		}

		@Override
		protected void applyValue() {
			ClientPlayNetworking.send(new SetOptionPayload(pos, index, false, actual()));
		}
	}
}
