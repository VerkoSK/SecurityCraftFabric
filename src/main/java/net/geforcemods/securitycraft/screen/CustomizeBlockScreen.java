package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.EnumMap;
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
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.SetOptionPayload;
import net.geforcemods.securitycraft.network.ToggleModulePayload;
import net.geforcemods.securitycraft.screen.components.CallbackSlider;
import net.geforcemods.securitycraft.screen.components.ColoredButton;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The Universal Block Modifier's screen: module slots and description icons inside the panel, options laid out
 * in a column to its right. Layout numbers, texture selection, the module enable/disable toggle and option
 * handling are 1:1 with upstream's {@code CustomizeBlockScreen}.
 */
public class CustomizeBlockScreen extends AbstractContainerScreen<CustomizeBlockMenu> implements IHasExtraAreas, ContainerListener {
	private static final Identifier BEACON_GUI = Identifier.withDefaultNamespace("textures/gui/container/beacon.png");
	private final List<Rect2i> extraAreas = new ArrayList<>();
	private final IModuleInventory moduleInv;
	private final BlockPos pos;
	private final int maxNumberOfModules;
	private final Identifier texture;
	private final Option<?>[] options;
	private final ModuleButton[] descriptionButtons;
	private final EnumMap<ModuleType, Boolean> indicators = new EnumMap<>(ModuleType.class);
	private AbstractWidget[] optionButtons;

	public CustomizeBlockScreen(CustomizeBlockMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		moduleInv = menu.moduleInv;
		pos = ((BlockEntity) moduleInv).getBlockPos();
		maxNumberOfModules = moduleInv.getMaxNumberOfModules();
		texture = SCContent.id("textures/gui/container/customize" + maxNumberOfModules + ".png");
		options = moduleInv instanceof ICustomizable customizable ? customizable.customOptions() : new Option<?>[0];
		descriptionButtons = new ModuleButton[maxNumberOfModules];
		menu.addSlotListener(this);

		for (ModuleType type : ModuleType.values()) {
			//a module that is not in the block yet counts as enabled, so it lights up the moment it is inserted
			indicators.put(type, !moduleInv.hasModule(type) || moduleInv.isModuleEnabled(type));
		}
	}

	@Override
	protected void init() {
		super.init();
		extraAreas.clear();

		final int numberOfColumns = 2;

		for (int i = 0; i < maxNumberOfModules; i++) {
			int column = i % numberOfColumns;
			ModuleType type = moduleInv.acceptedModules()[i];
			ModuleItem moduleItem = type.getItem();

			descriptionButtons[i] = addRenderableWidget(new ModuleButton(leftPos + 127 + column * 22, (topPos + 16) + (Math.floorDiv(i, numberOfColumns) * 22), 20, 20, moduleItem, this::moduleButtonClicked));
			descriptionButtons[i].active = moduleInv.hasModule(type);
			descriptionButtons[i].setTooltip(Tooltip.create(getModuleTooltipText(new ItemStack(moduleItem), moduleItem)));
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
				else {
					ColoredButton button = new ColoredButton(x, y, 120, 20, getOptionButtonTitle(option), b -> optionButtonClicked(index));

					button.setFGColor(optionColor(option));
					optionButtons[i] = button;
				}

				addRenderableWidget(optionButtons[i]);
				optionButtons[i].setTooltip(Tooltip.create(getOptionDescription(index)));
			}

			for (AbstractWidget button : optionButtons) {
				extraAreas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
			}
		}
	}

	private void moduleButtonClicked(Button button) {
		ModuleType moduleType = ((ModuleButton) button).getModule().getModuleType();

		//the client toggles right away so the indicator flips without waiting for the server to answer
		if (moduleInv.isModuleEnabled(moduleType)) {
			indicators.put(moduleType, false);
			moduleInv.removeModule(moduleType, true);
		}
		else {
			indicators.put(moduleType, true);
			moduleInv.insertModule(moduleInv.getModule(moduleType), true);
		}

		ClientPlayNetworking.send(new ToggleModulePayload(pos, moduleType));
	}

	/** Pale yellow while the option still holds its default value, light grey once it has been changed. */
	private static int optionColor(Option<?> option) {
		return option.toString().equals(option.getDefaultValue().toString()) ? 16777120 : 14737632;
	}

	private void optionButtonClicked(int index) {
		Option<?> tempOption = options[index];
		ColoredButton button = (ColoredButton) optionButtons[index];

		tempOption.toggle();
		button.setFGColor(optionColor(tempOption));
		button.setMessage(getOptionButtonTitle(tempOption));
		optionButtons[index].setTooltip(Tooltip.create(getOptionDescription(index)));
		ClientPlayNetworking.send(new SetOptionPayload(pos, index, true, 0.0));
	}

	@Override
	public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
		if (slotIndex < 36)
			return;

		//when a stack is taken out of a slot the module type cannot be read back reliably, so every accepted type is rechecked
		for (int i = 0; i < maxNumberOfModules; i++) {
			if (descriptionButtons[i] != null) {
				ModuleType type = moduleInv.acceptedModules()[i];

				descriptionButtons[i].active = moduleInv.hasModule(type);

				if (!descriptionButtons[i].active)
					indicators.remove(type);
				else
					indicators.computeIfAbsent(type, t -> true);
			}
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu menu, int slotIndex, int value) {}

	private Component getModuleTooltipText(ItemStack stack, ModuleItem moduleItem) {
		return Utils.localize(stack.getItem().getDescriptionId())
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

		//the green tick / red cross above every filled module slot, taken from the beacon screen's sprite sheet
		for (int i = 36; i < menu.getMaxSlots(); i++) {
			Slot slot = menu.slots.get(i);

			if (!slot.getItem().isEmpty() && slot.getItem().getItem() instanceof ModuleItem moduleItem) {
				ModuleType type = moduleItem.getModuleType();

				if (indicators.containsKey(type))
					guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BEACON_GUI, leftPos + slot.x - 2, topPos + slot.y + 16, indicators.get(type) ? 88 : 110, 219, 20, 20, 21, 22, 256, 256);
			}
		}

		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFF404040, false);
		guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	private class ModuleButton extends PictureButton {
		private final ModuleItem module;

		public ModuleButton(int xPos, int yPos, int width, int height, ModuleItem itemToRender, OnPress onPress) {
			super(xPos, yPos, width, height, new ItemStack(itemToRender), onPress);
			module = itemToRender;
		}

		public ModuleItem getModule() {
			return module;
		}
	}
}
