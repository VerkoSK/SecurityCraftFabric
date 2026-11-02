package net.geforcemods.securitycraft.screen;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity;
import net.geforcemods.securitycraft.inventory.ProjectorMenu;
import net.geforcemods.securitycraft.network.SyncProjectorPayload;
import net.geforcemods.securitycraft.network.SyncProjectorPayload.DataType;
import net.geforcemods.securitycraft.screen.components.CallbackCheckbox;
import net.geforcemods.securitycraft.screen.components.CallbackSlider;
import net.geforcemods.securitycraft.screen.components.StateSelector;
import net.geforcemods.securitycraft.screen.components.TextHoverChecker;
import net.geforcemods.securitycraft.screen.components.TogglePictureButton;
import net.geforcemods.securitycraft.util.IHasExtraAreas;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ProjectorScreen extends AbstractContainerScreen<ProjectorMenu> implements IHasExtraAreas {
	private static final Identifier TEXTURE = SCContent.id("textures/gui/container/projector.png");
	private static final Component SLOT_TOOLTIP = Utils.localize("gui.securitycraft:projector.block");
	private ProjectorBlockEntity be;
	private TextHoverChecker slotHoverChecker;
	private CallbackSlider projectionWidthSlider;
	private CallbackSlider projectionHeightSlider;
	private CallbackSlider projectionRangeSlider;
	private CallbackSlider projectionOffsetSlider;
	private TogglePictureButton horizontalToggleButton;
	private CallbackCheckbox overrideCheckbox;
	private StateSelector stateSelector;
	private int sliderWidth = 120;

	public ProjectorScreen(ProjectorMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 235);
		this.be = menu.be;
	}

	@Override
	public void init() {
		super.init();
		leftPos += 90;

		int left = leftPos + ((imageWidth - sliderWidth) / 2);

		projectionWidthSlider = addRenderableWidget(new CallbackSlider(left, topPos + 57, sliderWidth, 20, Utils.localize("gui.securitycraft:projector.width", ""), Component.empty(), ProjectorBlockEntity.MIN_WIDTH, ProjectorBlockEntity.MAX_WIDTH, be.getProjectionWidth(), true, this::applySliderValue));
		projectionWidthSlider.setFGColor(14737632);
		projectionWidthSlider.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:projector.width.description")));

		projectionHeightSlider = addRenderableWidget(new CallbackSlider(left, topPos + 78, sliderWidth, 20, Utils.localize("gui.securitycraft:projector.height", ""), Component.empty(), ProjectorBlockEntity.MIN_WIDTH, ProjectorBlockEntity.MAX_WIDTH, be.getProjectionHeight(), true, this::applySliderValue));
		projectionHeightSlider.setFGColor(14737632);
		projectionHeightSlider.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:projector.height.description")));

		projectionRangeSlider = addRenderableWidget(new CallbackSlider(left, topPos + 99, sliderWidth, 20, Utils.localize("gui.securitycraft:projector.range", ""), Component.empty(), ProjectorBlockEntity.MIN_RANGE - (be.isHorizontal() ? 16 : 0), ProjectorBlockEntity.MAX_RANGE - (be.isHorizontal() ? 16 : 0), be.getProjectionRange() - (be.isHorizontal() ? 16 : 0), true, this::applySliderValue));
		projectionRangeSlider.setFGColor(0xE0E0E0);
		projectionRangeSlider.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:projector.range.description")));

		projectionOffsetSlider = addRenderableWidget(new CallbackSlider(left, topPos + 120, sliderWidth, 20, Utils.localize("gui.securitycraft:projector.offset", ""), Component.empty(), ProjectorBlockEntity.MIN_OFFSET, ProjectorBlockEntity.MAX_OFFSET, be.getProjectionOffset(), true, this::applySliderValue));
		projectionOffsetSlider.setFGColor(14737632);
		projectionOffsetSlider.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:projector.offset.description")));

		horizontalToggleButton = addRenderableWidget(new TogglePictureButton(left + sliderWidth - 20, topPos + 36, 20, 20, TEXTURE, new int[]{176, 192}, new int[]{0, 0}, 2, 2, button -> {
			boolean horizontal = !be.isHorizontal();
			double oldValue = projectionRangeSlider.getValue();

			be.setHorizontal(horizontal);
			updateHorizontalToggleButtonTooltip();
			projectionRangeSlider.setMinValue(projectionRangeSlider.getMinValue() - (horizontal ? 16 : -16));
			projectionRangeSlider.setMaxValue(projectionRangeSlider.getMaxValue() - (horizontal ? 16 : -16));
			projectionRangeSlider.setValue(oldValue - (horizontal ? 16 : -16));
			applySliderValue(projectionRangeSlider);
			ClientPlayNetworking.send(new SyncProjectorPayload(be.getBlockPos(), be.isHorizontal() ? 1 : 0, DataType.HORIZONTAL));
		}));
		horizontalToggleButton.setCurrentIndex(be.isHorizontal() ? 1 : 0);
		updateHorizontalToggleButtonTooltip();

		overrideCheckbox = addRenderableWidget(new CallbackCheckbox(left + sliderWidth - 41, topPos + 36, 20, 20, Component.empty(), be.isOverridingBlocks(), newValue -> {
			be.setOverridingBlocks(newValue);
			ClientPlayNetworking.send(new SyncProjectorPayload(be.getBlockPos(), be.isOverridingBlocks() ? 1 : 0, DataType.OVERRIDING_BLOCKS));
			updateOverrideCheckboxTooltip();
		}, 0));
		updateOverrideCheckboxTooltip();

		slotHoverChecker = new TextHoverChecker(topPos + 22, topPos + 39, leftPos + 78, leftPos + 95, SLOT_TOOLTIP);

		stateSelector = addRenderableWidget(new StateSelector(menu, title, leftPos - 190, topPos + 7, 0, 197, 0, 234, 35));
		stateSelector.init(width, height);
	}

	private void updateHorizontalToggleButtonTooltip() {
		horizontalToggleButton.setTooltip(Tooltip.create(be.isHorizontal() ? Utils.localize("gui.securitycraft:projector.horizontal") : Utils.localize("gui.securitycraft:projector.vertical")));
	}

	private void updateOverrideCheckboxTooltip() {
		overrideCheckbox.setTooltip(Tooltip.create(be.isOverridingBlocks() ? Utils.localize("gui.securitycraft:projector.isOverridingBlocks.yes") : Utils.localize("gui.securitycraft:projector.isOverridingBlocks.no")));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
		extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
		super.extractRenderState(extractor, mouseX, mouseY, partialTicks);

		if (slotHoverChecker.checkHover(mouseX, mouseY) && menu.be.isEmpty())
			extractor.setTooltipForNextFrame(font, slotHoverChecker.getName(), mouseX, mouseY);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
		extractor.text(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFF404040, false);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (stateSelector != null && stateSelector.mouseDragged(event, dragX, dragY))
			return true;

		return (getFocused() != null && isDragging() && event.button() == 0 && getFocused().mouseDragged(event, dragX, dragY)) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		if (stateSelector != null)
			return stateSelector.getGuiExtraAreas();
		else
			return List.of();
	}

	@Override
	public void onClose() {
		if (stateSelector != null)
			stateSelector.onClose();
		super.onClose();

		if (stateSelector != null && stateSelector.getState() != null) {
			be.setProjectedState(stateSelector.getState());
			ClientPlayNetworking.send(new SyncProjectorPayload(be.getBlockPos(), stateSelector.getState()));
		}
	}

	public void applySliderValue(CallbackSlider slider) {
		int data = 0;
		DataType dataType = DataType.INVALID;

		if (slider == projectionWidthSlider) {
			data = slider.getValueInt();
			be.setProjectionWidth(data);
			dataType = DataType.WIDTH;
		}
		else if (slider == projectionHeightSlider) {
			data = slider.getValueInt();
			be.setProjectionHeight(data);
			dataType = DataType.HEIGHT;
		}
		else if (slider == projectionRangeSlider) {
			data = slider.getValueInt();

			if (be.isHorizontal())
				data += 16;

			be.setProjectionRange(data);
			dataType = DataType.RANGE;
		}
		else if (slider == projectionOffsetSlider) {
			data = slider.getValueInt();
			be.setProjectionOffset(data);
			dataType = DataType.OFFSET;
		}

		ClientPlayNetworking.send(new SyncProjectorPayload(be.getBlockPos(), data, dataType));
	}
}
