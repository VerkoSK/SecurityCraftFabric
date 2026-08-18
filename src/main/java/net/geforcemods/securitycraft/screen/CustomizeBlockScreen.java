package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DoubleOption;
import net.geforcemods.securitycraft.api.Option.EntityDataWrappedOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.inventory.CustomizeBlockMenu;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.network.SetOptionPayload;
import net.geforcemods.securitycraft.screen.components.CallbackSlider;
import net.geforcemods.securitycraft.screen.components.PictureButton;
import net.geforcemods.securitycraft.util.IHasExtraAreas;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The Universal Block Modifier's screen: module slots and description icons inside the panel, options laid out
 * in a column to its right. Layout numbers, texture selection and option handling are 1:1 with upstream's
 * {@code CustomizeBlockScreen}, minus the module enable/disable toggle (upstream's {@code ToggleModule} packet
 * has no port equivalent, so the module icons are description-only here; see class notes on the port task).
 */
public class CustomizeBlockScreen extends AbstractContainerScreen<CustomizeBlockMenu> implements IHasExtraAreas {
	private final List<Rect2i> extraAreas = new ArrayList<>();
	private final IModuleInventory moduleInv;
	private final BlockPos pos;
	private final int maxNumberOfModules;
	private final ResourceLocation texture;
	private final Option<?>[] options;
	private AbstractWidget[] optionButtons;

	public CustomizeBlockScreen(CustomizeBlockMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		moduleInv = menu.moduleInv;
		pos = ((BlockEntity) moduleInv).getBlockPos();
		maxNumberOfModules = moduleInv.getMaxNumberOfModules();
		texture = SCContent.id("textures/gui/container/customize" + maxNumberOfModules + ".png");
		options = moduleInv instanceof ICustomizable customizable ? customizable.customOptions() : new Option<?>[0];
	}

	@Override
	protected void init() {
		super.init();
		extraAreas.clear();

		final int numberOfColumns = 2;

		for (int i = 0; i < maxNumberOfModules; i++) {
			int column = i % numberOfColumns;
			ModuleItem moduleItem = (ModuleItem) moduleInv.acceptedModules()[i].getItem();
			ItemStack stack = new ItemStack(moduleItem);
			PictureButton button = addRenderableWidget(new PictureButton(leftPos + 127 + column * 22, (topPos + 16) + (Math.floorDiv(i, numberOfColumns) * 22), 20, 20, stack, b -> {}));

			button.active = moduleInv.hasModule(moduleItem.getModuleType());
			button.setTooltip(Tooltip.create(getModuleTooltipText(stack, moduleItem)));
		}

		if (options.length > 0) {
			optionButtons = new AbstractWidget[options.length];

			for (int i = 0; i < options.length; i++) {
				int index = i;
				Option<?> option = options[i] instanceof EntityDataWrappedOption<?> wrapped ? wrapped.getWrapped() : options[i];
				int x = leftPos + imageWidth + 2;
				int y = topPos + 10 + i * 25;

				if (option.isSlider()) {
					CallbackSlider slider;

					if (option instanceof DoubleOption doubleOption) {
						slider = new CallbackSlider(x, y, 120, 20, doubleOption.getMin(), doubleOption.getMax(), doubleOption.get(), doubleOption.getIncrement(), s -> {
							doubleOption.setValue(s.getValue());
							optionButtons[index].setTooltip(Tooltip.create(getOptionDescription(index)));
							ClientPlayNetworking.send(new SetOptionPayload(pos, index, false, doubleOption.get()));
						}) {
							@Override
							protected void updateMessage() {
								setMessage(getOptionButtonTitle(option));
							}
						};
					}
					else {
						IntOption intOption = (IntOption) option;

						slider = new CallbackSlider(x, y, 120, 20, intOption.getMin(), intOption.getMax(), intOption.get(), 1.0, s -> {
							intOption.setValue(s.getValueInt());
							optionButtons[index].setTooltip(Tooltip.create(getOptionDescription(index)));
							ClientPlayNetworking.send(new SetOptionPayload(pos, index, false, intOption.get()));
						}) {
							@Override
							protected void updateMessage() {
								setMessage(getOptionButtonTitle(option));
							}
						};
					}

					optionButtons[i] = slider;
				}
				else
					optionButtons[i] = Button.builder(getOptionButtonTitle(option), b -> optionButtonClicked(index)).bounds(x, y, 120, 20).build();

				addRenderableWidget(optionButtons[i]);
				optionButtons[i].setTooltip(Tooltip.create(getOptionDescription(index)));
			}

			for (AbstractWidget button : optionButtons) {
				extraAreas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
			}
		}
	}

	private void optionButtonClicked(int index) {
		Option<?> tempOption = options[index];
		Button button = (Button) optionButtons[index];

		tempOption.toggle();
		button.setMessage(getOptionButtonTitle(tempOption));
		optionButtons[index].setTooltip(Tooltip.create(getOptionDescription(index)));
		ClientPlayNetworking.send(new SetOptionPayload(pos, index, true, 0.0));
	}

	private Component getModuleTooltipText(ItemStack stack, ModuleItem moduleItem) {
		return Utils.localize(stack.getDescriptionId())
				.append(Component.literal(":"))
				.withStyle(ChatFormatting.RESET)
				.append(Component.literal("\n\n"))
				.append(Utils.localize(moduleInv.getModuleDescriptionId(Utils.getLanguageKeyDenotation(moduleInv), moduleItem.getModuleType())));
	}

	private Component getOptionDescription(int optionId) {
		Option<?> option = options[optionId];

		return Utils.localize("gui.securitycraft:customize.tooltip", Component.translatable(option.getDescriptionKey(Utils.getLanguageKeyDenotation(moduleInv))), Component.translatable("gui.securitycraft:customize.currentSetting", option.getValueText()));
	}

	private Component getOptionButtonTitle(Option<?> option) {
		return Utils.localize(option.getKey(Utils.getLanguageKeyDenotation(moduleInv)), option.getValueText());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFF404040, false);
		guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}
}
