package net.geforcemods.securitycraft.screen;

import java.util.Optional;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.inventory.KeycardReaderMenu;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.network.SetKeycardUsesPayload;
import net.geforcemods.securitycraft.network.SyncKeycardSettingsPayload;
import net.geforcemods.securitycraft.screen.components.ActiveBasedTextureButton;
import net.geforcemods.securitycraft.screen.components.TogglePictureButton;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The keycard reader/lock's programming screen: put an unlinked (or your own already-linked) keycard in the slot,
 * pick which levels it should open, optionally restrict it to one player's name, then Link. Laid out to match
 * upstream's screen 1:1 (signature field + stepper row, level toggles, usable-by field, uses field + return
 * button, link button, a top-left Smart Module indicator). Matches upstream's Smart Module gate too: without one
 * installed, level toggling is restricted to "exactly this level" (default) or "this level and every one above it"
 * (the "=" / ">=" button, upstream-style); with one installed, each level toggles independently and that button
 * disappears. The level toggle icons are this port's own (1.20.1 predates the per-sprite gui/sprites/...
 * convention vanilla's beacon confirm/cancel icon would need, so copying it by UV offset from the old spritesheet
 * wasn't worth the risk of getting the offset wrong).
 */
public class KeycardReaderScreen extends AbstractContainerScreen<KeycardReaderMenu> {
	private static final Identifier TEXTURE = SCContent.id("textures/gui/container/keycard_reader.png");
	private static final Identifier LEVEL_CONFIRM_SPRITE = SCContent.id("widget/level_confirm");
	private static final Identifier LEVEL_CANCEL_SPRITE = SCContent.id("widget/level_cancel");
	private static final Identifier RANDOM_SPRITE = SCContent.id("widget/random");
	private static final Identifier RANDOM_INACTIVE_SPRITE = SCContent.id("widget/random_inactive");
	private static final Identifier RESET_SPRITE = SCContent.id("widget/reset");
	private static final Identifier RESET_INACTIVE_SPRITE = SCContent.id("widget/reset_inactive");
	private static final Identifier RETURN_SPRITE = SCContent.id("widget/return");
	private static final Identifier RETURN_INACTIVE_SPRITE = SCContent.id("widget/return_inactive");
	private static final Component EQUALS = Component.literal("=");
	private static final Component GREATER_EQUALS = Component.literal(">=");
	private static final int MAX_SIGNATURE = 99999;
	private static final java.util.Random RANDOM = new java.util.Random();

	private final Component signatureText = Utils.localize("gui.securitycraft:keycard_reader.signature");
	private final KeycardReaderBlockEntity be;
	private final boolean isOwner;
	private final boolean hasSmartModule;
	private final Component smartModuleTooltip;
	private boolean[] acceptedLevels;
	private boolean isExactLevel = true;
	private int signature;
	private int previousSignature;
	private int signatureTextStartX;
	private final TogglePictureButton[] levelBoxes = new TogglePictureButton[5];
	private EditBox signatureField, usableByField, usesField;
	private Button minusThree, minusTwo, minusOne, reset, plusOne, plusTwo, plusThree, randomize;
	private Button linkButton;
	private ActiveBasedTextureButton setUsesButton;
	private boolean firstTick = true;

	public KeycardReaderScreen(KeycardReaderMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 249);
		be = menu.be;
		acceptedLevels = be.getAcceptedLevels().clone();
		previousSignature = signature = be.getSignature();
		isOwner = be.isOwnedBy(inv.player);
		hasSmartModule = be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.SMART);
		smartModuleTooltip = Utils.localize(hasSmartModule ? "gui.securitycraft:keycard_reader.smartModule" : "gui.securitycraft:keycard_reader.noSmartModule");
	}

	@Override
	public void init() {
		super.init();

		signatureTextStartX = imageWidth / 2 - font.width(signatureText) + 5;

		int buttonY = topPos + 35;

		signatureField = addRenderableWidget(new EditBox(font, leftPos + 96, topPos + 21, 40, 12, Component.empty()));
		signatureField.setValue(leftPaddedSignature());
		signatureField.setMaxLength(5);
		signatureField.setEditable(isOwner);
		signatureField.setResponder(text -> {
			String digits = text.replaceAll("\\D", "");

			if (digits.length() > 5)
				digits = digits.substring(0, 5);
			if (!digits.equals(text))
				signatureField.setValue(digits);
			else
				changeSignatureFromField(digits);
		});

		minusThree = addRenderableWidget(Button.builder(Component.literal("---"), b -> changeSignature(signature - 100)).bounds(leftPos + 22, buttonY, 24, 13).build());
		minusTwo = addRenderableWidget(Button.builder(Component.literal("--"), b -> changeSignature(signature - 10)).bounds(leftPos + 48, buttonY, 18, 13).build());
		minusOne = addRenderableWidget(Button.builder(Component.literal("-"), b -> changeSignature(signature - 1)).bounds(leftPos + 68, buttonY, 12, 13).build());
		reset = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 82, buttonY, 12, 13, RESET_SPRITE, RESET_INACTIVE_SPRITE, 1, 1, 10, 10, b -> changeSignature(previousSignature)));
		plusOne = addRenderableWidget(Button.builder(Component.literal("+"), b -> changeSignature(signature + 1)).bounds(leftPos + 96, buttonY, 12, 13).build());
		plusTwo = addRenderableWidget(Button.builder(Component.literal("++"), b -> changeSignature(signature + 10)).bounds(leftPos + 110, buttonY, 18, 13).build());
		plusThree = addRenderableWidget(Button.builder(Component.literal("+++"), b -> changeSignature(signature + 100)).bounds(leftPos + 130, buttonY, 24, 13).build());
		randomize = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 156, buttonY, 12, 13, RANDOM_SPRITE, RANDOM_INACTIVE_SPRITE, 1, 1, 10, 10, b -> changeSignature(RANDOM.nextInt(MAX_SIGNATURE))));

		for (Button b : new Button[] {
				minusThree, minusTwo, minusOne, reset, plusOne, plusTwo, plusThree, randomize
		}) {
			b.active = isOwner;
		}

		for (int i = 0; i < 5; i++) {
			int index = i;
			int y = topPos + 50 + (i + 1) * 17;

			levelBoxes[i] = addRenderableWidget(new TogglePictureButton(leftPos + 100, y, 15, 15, 2, 2, 11, 11, LEVEL_CANCEL_SPRITE, LEVEL_CONFIRM_SPRITE, acceptedLevels[i], selected -> onLevelToggled(index, selected)));
			levelBoxes[i].active = isOwner;
		}

		//only meaningful without a Smart Module: with one, every level toggles independently and there is no mode to pick
		if (!hasSmartModule) {
			Button exactLevelButton = addRenderableWidget(Button.builder(isExactLevel ? EQUALS : GREATER_EQUALS, b -> {
				isExactLevel = !isExactLevel;
				b.setMessage(isExactLevel ? EQUALS : GREATER_EQUALS);
			}).bounds(leftPos + 135, topPos + 67, 18, 18).build());
			exactLevelButton.active = isOwner;
		}

		usableByField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 66, 70, 15, Component.empty()));
		usableByField.setHint(Utils.localize("gui.securitycraft:keycard_reader.usable_by.hint"));
		usableByField.setMaxLength(16);
		usableByField.setEditable(isOwner);

		linkButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:keycard_reader.link"), b -> {
			ClientPlayNetworking.send(new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, true, getUsableBy()));
		}).bounds(leftPos + 8, topPos + 126, 70, 20).build());
		linkButton.active = false;

		usesField = addRenderableWidget(new EditBox(font, leftPos + 28, topPos + 107, 30, 15, Component.empty()));
		usesField.setMaxLength(4);
		usesField.setResponder(text -> {
			String digits = text.replaceAll("\\D", "");

			if (digits.length() > 4)
				digits = digits.substring(0, 4);
			if (!digits.equals(text))
				usesField.setValue(digits);
		});

		setUsesButton = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 62, topPos + 106, 16, 17, RETURN_SPRITE, RETURN_INACTIVE_SPRITE, 2, 2, 14, 14, b -> {
			if (!usesField.getValue().isEmpty())
				ClientPlayNetworking.send(new SetKeycardUsesPayload(be.getBlockPos(), Integer.parseInt(usesField.getValue())));
		}));
		setUsesButton.active = false;

		//set the correct active/inactive state for the stepper buttons up front, instead of only after the first click
		changeSignature(signature);
	}

	/**
	 * With a Smart Module installed, each level is an independent on/off toggle. Without one, upstream restricts it
	 * to either "=" (exactly this level - clicking one unchecks every other) or ">=" (this level and every one
	 * above it), picked with the button next to the list.
	 */
	private void onLevelToggled(int index, boolean selected) {
		if (hasSmartModule) {
			setLevelState(index, selected);
			return;
		}

		if (isExactLevel) {
			for (int i = 0; i < 5; i++) {
				setLevelState(i, i == index);
			}
		}
		else if (selected) {
			for (int i = index; i < 5; i++) {
				setLevelState(i, true);
			}
		}
		else
			setLevelState(index, false);
	}

	private void setLevelState(int index, boolean active) {
		acceptedLevels[index] = active;
		levelBoxes[index].setCurrentIndex(active ? 1 : 0);
	}

	private void changeSignatureFromField(String value) {
		if (!value.isEmpty())
			changeSignature(Integer.parseInt(value), true);
	}

	private void changeSignature(int newSignature) {
		changeSignature(newSignature, false);
	}

	private void changeSignature(int newSignature, boolean throughField) {
		if (!isOwner)
			return;

		signature = Mth.clamp(newSignature, 0, MAX_SIGNATURE);
		minusThree.active = minusTwo.active = minusOne.active = signature != 0;
		plusOne.active = plusTwo.active = plusThree.active = signature != MAX_SIGNATURE;
		reset.active = signature != previousSignature;

		if (!throughField)
			signatureField.setValue(leftPaddedSignature());
	}

	private String leftPaddedSignature() {
		return String.format("%05d", signature);
	}

	private Optional<String> getUsableBy() {
		String value = usableByField.getValue();

		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
		extractor.text(font, title, imageWidth / 2 - font.width(title) / 2, 6, 4210752, false);
		extractor.text(font, signatureText, signatureTextStartX, 23, 4210752, false);

		Component keycardLevels = Utils.localize("gui.securitycraft:keycard_reader.keycard_levels");

		extractor.text(font, keycardLevels, 170 - font.width(keycardLevels), 56, 4210752, false);

		for (int i = 1; i <= 5; i++) {
			extractor.text(font, "" + i, 91, 55 + 17 * i, 4210752, false);
		}

		extractor.text(font, playerInventoryTitle, 8, imageHeight - 93, 4210752, false);
	}

	@Override
	public void containerTick() {
		super.containerTick();

		ItemStack stack = menu.keycardSlot.getItem();
		boolean isEmpty = stack.isEmpty();
		boolean isLimited = !isEmpty && KeycardItem.isLimited(stack);

		usesField.setEditable(isLimited);
		usesField.active = isLimited;

		if (usesField.active && usesField.getValue().isEmpty())
			usesField.setValue("" + KeycardItem.getUsesLeft(stack));
		else if (!usesField.active && !usesField.getValue().isEmpty())
			usesField.setValue("");

		if (firstTick) {
			linkButton.active = false;
			setUsesButton.active = false;
			firstTick = false;
			return;
		}

		linkButton.active = isOwner && !isEmpty;
		setUsesButton.active = isLimited && !usesField.getValue().isEmpty();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);
		net.geforcemods.securitycraft.util.ClientUtils.renderModuleInfo(extractor, font, net.geforcemods.securitycraft.misc.ModuleType.SMART, smartModuleTooltip, hasSmartModule, leftPos + 5, topPos + 5, mouseX, mouseY);
	}

	@Override
	public void removed() {
		super.removed();

		if (isOwner)
			ClientPlayNetworking.send(new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, false, getUsableBy()));
	}
}
