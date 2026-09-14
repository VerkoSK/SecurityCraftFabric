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
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The keycard reader/lock's programming screen: put an unlinked (or your own already-linked) keycard in the slot,
 * pick which levels it should open, optionally restrict it to one player's name, then Link. Laid out to match
 * upstream's screen 1:1 (same coordinates: signature field + stepper row, level toggles on the right, usable-by
 * field, uses field + return button, link button); simplified in one way: the level toggles are independent
 * checkboxes here (no Smart Module "exact level" vs "this level and up" distinction - the port's Keycard Reader
 * doesn't model that) and they use a plain check/cross glyph instead of copying vanilla's beacon icon by UV offset
 * (1.20.1 predates the per-sprite gui/sprites/... convention that icon would need).
 */
public class KeycardReaderScreen extends AbstractContainerScreen<KeycardReaderMenu> {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/keycard_reader.png");
	private static final Component CHECK = Component.literal("✔").withStyle(net.minecraft.ChatFormatting.GREEN);
	private static final Component CROSS = Component.literal("✖").withStyle(net.minecraft.ChatFormatting.RED);
	private static final ResourceLocation RANDOM_SPRITE = SCContent.id("textures/gui/sprites/widget/random.png");
	private static final ResourceLocation RANDOM_INACTIVE_SPRITE = SCContent.id("textures/gui/sprites/widget/random_inactive.png");
	private static final ResourceLocation RESET_SPRITE = SCContent.id("textures/gui/sprites/widget/reset.png");
	private static final ResourceLocation RESET_INACTIVE_SPRITE = SCContent.id("textures/gui/sprites/widget/reset_inactive.png");
	private static final ResourceLocation RETURN_SPRITE = SCContent.id("textures/gui/sprites/widget/return.png");
	private static final ResourceLocation RETURN_INACTIVE_SPRITE = SCContent.id("textures/gui/sprites/widget/return_inactive.png");
	private static final int MAX_SIGNATURE = 99999;
	private static final java.util.Random RANDOM = new java.util.Random();

	private final Component signatureText = Utils.localize("gui.securitycraft:keycard_reader.signature");
	private final KeycardReaderBlockEntity be;
	private final boolean isOwner;
	private boolean[] acceptedLevels;
	private int signature;
	private int previousSignature;
	private int signatureTextStartX;
	private final Button[] levelBoxes = new Button[5];
	private EditBox signatureField, usableByField, usesField;
	private Button minusThree, minusTwo, minusOne, reset, plusOne, plusTwo, plusThree, randomize;
	private Button linkButton;
	private ActiveBasedTextureButton setUsesButton;
	private boolean firstTick = true;

	public KeycardReaderScreen(KeycardReaderMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		be = menu.be;
		acceptedLevels = be.getAcceptedLevels().clone();
		previousSignature = signature = be.getSignature();
		isOwner = be.isOwnedBy(inv.player);
		imageWidth = 176;
		imageHeight = 249;
	}

	@Override
	public void init() {
		super.init();

		signatureTextStartX = imageWidth / 2 - font.width(signatureText) + 5;

		int buttonY = topPos + 35;

		signatureField = addRenderableWidget(new EditBox(font, leftPos + 96, topPos + 21, 40, 12, Component.empty()));
		signatureField.setValue(leftPaddedSignature());
		signatureField.setFilter(s -> s.matches("\\d{0,5}"));
		signatureField.setMaxLength(5);
		signatureField.setEditable(isOwner);
		signatureField.setResponder(this::changeSignatureFromField);

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

			levelBoxes[i] = addRenderableWidget(Button.builder(acceptedLevels[i] ? CHECK : CROSS, b -> {
				acceptedLevels[index] = !acceptedLevels[index];
				b.setMessage(acceptedLevels[index] ? CHECK : CROSS);
			}).bounds(leftPos + 100, y, 15, 15).build());
			levelBoxes[i].active = isOwner;
		}

		usableByField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 66, 70, 15, Component.empty()));
		usableByField.setHint(Utils.localize("gui.securitycraft:keycard_reader.usable_by.hint"));
		usableByField.setMaxLength(16);
		usableByField.setEditable(isOwner);

		linkButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:keycard_reader.link"), b -> {
			ClientPlayNetworking.send(SyncKeycardSettingsPayload.CHANNEL, new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, true, getUsableBy()).write());
		}).bounds(leftPos + 8, topPos + 126, 70, 20).build());
		linkButton.active = false;

		usesField = addRenderableWidget(new EditBox(font, leftPos + 28, topPos + 107, 30, 15, Component.empty()));
		usesField.setFilter(s -> s.matches("\\d*"));
		usesField.setMaxLength(4);

		setUsesButton = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 62, topPos + 106, 16, 17, RETURN_SPRITE, RETURN_INACTIVE_SPRITE, 2, 2, 14, 14, b -> {
			if (!usesField.getValue().isEmpty())
				ClientPlayNetworking.send(SetKeycardUsesPayload.CHANNEL, new SetKeycardUsesPayload(be.getBlockPos(), Integer.parseInt(usesField.getValue())).write());
		}));
		setUsesButton.active = false;
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
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 4210752, false);
		guiGraphics.drawString(font, signatureText, signatureTextStartX, 23, 4210752, false);

		Component keycardLevels = Utils.localize("gui.securitycraft:keycard_reader.keycard_levels");

		guiGraphics.drawString(font, keycardLevels, 170 - font.width(keycardLevels), 56, 4210752, false);

		for (int i = 1; i <= 5; i++) {
			guiGraphics.drawString(font, "" + i, 91, 55 + 17 * i, 4210752, false);
		}

		guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 93, 4210752, false);
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void removed() {
		super.removed();

		if (isOwner)
			ClientPlayNetworking.send(SyncKeycardSettingsPayload.CHANNEL, new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, false, getUsableBy()).write());
	}
}
