package net.geforcemods.securitycraft.screen;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCClientConfig;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.inventory.BlockReinforcerMenu;
import net.geforcemods.securitycraft.misc.TintMode;
import net.geforcemods.securitycraft.network.SyncBlockReinforcerPayload;
import net.geforcemods.securitycraft.screen.components.ActiveBasedTextureButton;
import net.geforcemods.securitycraft.screen.components.ColorChooser;
import net.geforcemods.securitycraft.screen.components.ColorChooserButton;
import net.geforcemods.securitycraft.screen.components.ToggleComponentButton;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.IHasExtraAreas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Screen for the Universal Block Reinforcer: mode toggle + tint mode/colour/save row, matching the original SecurityCraft GUI. */
public class BlockReinforcerScreen extends AbstractContainerScreen<BlockReinforcerMenu> implements IHasExtraAreas {
	private static final Identifier TEXTURE = SCContent.id("textures/gui/container/universal_block_reinforcer.png");
	private static final Identifier SAVE_SPRITE = SCContent.id("widget/save");
	private static final Identifier SAVE_INACTIVE_SPRITE = SCContent.id("widget/save_inactive");
	private final Component output = Component.translatable("gui.securitycraft:blockReinforcer.output");
	private final Component modeText = Component.translatable("gui.securitycraft:blockReinforcer.mode");
	private final Component tintText = Component.translatable("gui.securitycraft:blockReinforcer.tint");
	private ToggleComponentButton reinforcingModeButton;
	private ToggleComponentButton tintModeButton;
	private ColorChooser tintColorChooser;
	private ColorChooserButton colorChooserButton;
	private ActiveBasedTextureButton saveToConfigButton;
	private int oldTintColor;
	private TintMode oldTintMode;

	public BlockReinforcerScreen(BlockReinforcerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 186);
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		int tintRowY = topPos + 67;

		oldTintColor = TintMode.color();
		oldTintMode = TintMode.mode();
		reinforcingModeButton = addRenderableWidget(new ToggleComponentButton(leftPos + 89, topPos + 42, 80, 20, this::updateReinforcingModeButtonText, menu.isReinforcing() ? 0 : 1, 2, this::reinforcingModeButtonClicked));
		tintModeButton = addRenderableWidget(new ToggleComponentButton(leftPos + 44, tintRowY, 75, 20, i -> TintMode.values()[i].translate(), oldTintMode.ordinal(), TintMode.values().length, this::tintModeButtonClicked));
		tintColorChooser = new ColorChooser(Component.empty(), leftPos + 124, tintRowY, oldTintColor, this::colorChanged);
		colorChooserButton = addRenderableWidget(new ColorChooserButton(leftPos + 124, tintRowY, 20, 20, tintColorChooser, this::onColorChooserToggled));
		saveToConfigButton = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 149, tintRowY, 20, 20, SAVE_SPRITE, SAVE_INACTIVE_SPRITE, 2, 2, 16, 16, this::saveButtonClicked));

		updateReinforcingTooltip(menu.isReinforcing());
		updateTintTooltip(TintMode.mode());
		colorChooserButton.setTooltip(Tooltip.create(Component.translatable("gui.securitycraft:blockReinforcer.chooseTintColorTooltip")));
		updateSaveButton();

		if (!menu.canToggleMode())
			reinforcingModeButton.active = false;
	}

	private void onColorChooserToggled() {
		if (!tintColorChooser.disabled)
			tintColorChooser.init(width, height);
	}

	private Component updateReinforcingModeButtonText(int index) {
		return Component.translatable(index == 1 ? "gui.securitycraft:blockReinforcer.unreinforcing" : "gui.securitycraft:blockReinforcer.reinforcing");
	}

	private void reinforcingModeButtonClicked(Button button) {
		if (button instanceof ToggleComponentButton toggleButton) {
			boolean isReinforcing = toggleButton.getCurrentIndex() == 0;

			updateReinforcingTooltip(isReinforcing);
			ClientPlayNetworking.send(new SyncBlockReinforcerPayload(isReinforcing));
		}
	}

	private void tintModeButtonClicked(Button button) {
		if (button instanceof ToggleComponentButton toggleButton) {
			TintMode newMode = TintMode.values()[toggleButton.getCurrentIndex()];

			TintMode.setMode(newMode);
			updateTintTooltip(newMode);
			updateSaveButton();
		}
	}

	private void colorChanged(int newColor) {
		TintMode.setColor(newColor);
		updateSaveButton();
	}

	private void saveButtonClicked(Button button) {
		SCClientConfig.save();
		updateSaveButton();
	}

	private void updateReinforcingTooltip(boolean isReinforcing) {
		reinforcingModeButton.setTooltip(Tooltip.create(Component.translatable(isReinforcing ? "gui.securitycraft:blockReinforcer.reinforcing.tooltip" : "gui.securitycraft:blockReinforcer.unreinforcing.tooltip")));
	}

	private void updateTintTooltip(TintMode tintMode) {
		tintModeButton.setTooltip(Tooltip.create(tintMode.tooltip()));
	}

	private void updateSaveButton() {
		if (TintMode.mode() != SCClientConfig.tintMode || TintMode.color() != SCClientConfig.tintColor) {
			saveToConfigButton.active = true;
			saveToConfigButton.setTooltip(Tooltip.create(Component.translatable("gui.securitycraft:blockReinforcer.saveToConfigTooltip")));
		}
		else {
			saveToConfigButton.active = false;
			saveToConfigButton.setTooltip(null);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);

		if (!tintColorChooser.disabled) {
			tintColorChooser.extractBackground(extractor, mouseX, mouseY, partialTick);
			tintColorChooser.extractRenderState(extractor, mouseX, mouseY, partialTick);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
		extractor.text(font, title, (imageWidth - font.width(title)) / 2, 5, 0x404040, false);
		extractor.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

		if (!menu.getResult().isEmpty())
			extractor.text(font, output, 128 - font.width(output), 25, 0x404040, false);

		extractor.text(font, modeText, 8, 48, 0x404040, false);
		extractor.text(font, tintText, 8, 73, 0x404040, false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!tintColorChooser.disabled) {
			if (colorChooserButton.isMouseOver(event.x(), event.y()))
				return super.mouseClicked(event, doubleClick);

			tintColorChooser.mouseClicked(event, doubleClick);
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (!tintColorChooser.disabled)
			return tintColorChooser.mouseDragged(event, dragX, dragY);

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (!tintColorChooser.disabled)
			return tintColorChooser.mouseReleased(event);

		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!tintColorChooser.disabled)
			return tintColorChooser.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!tintColorChooser.disabled)
			return tintColorChooser.keyPressed(event);

		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!tintColorChooser.disabled)
			return tintColorChooser.charTyped(event);

		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		super.onClose();

		if (TintMode.mode() != oldTintMode || TintMode.color() != oldTintColor)
			ClientUtils.recompileAllChunksInRange();
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return tintColorChooser != null ? tintColorChooser.getGuiExtraAreas() : List.of();
	}
}
