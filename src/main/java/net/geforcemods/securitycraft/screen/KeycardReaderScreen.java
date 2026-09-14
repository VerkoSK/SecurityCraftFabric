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
 * upstream's screen (signature stepper row, red X / green check level toggles); simplified in one way: the level
 * toggles are independent checkboxes here (no Smart Module "exact level" vs "this level and up" distinction - the
 * port's Keycard Reader doesn't model that).
 */
public class KeycardReaderScreen extends AbstractContainerScreen<KeycardReaderMenu> {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/keycard_reader.png");
	//1.20.1 predates the per-sprite gui/sprites/... convention (beacon's confirm/cancel icons live baked into one
	//sheet there), so the level toggles use a plain check/cross glyph instead of copying vanilla's icon by UV offset
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

	private final KeycardReaderBlockEntity be;
	private final boolean isOwner;
	private boolean[] acceptedLevels;
	private int signature;
	private int previousSignature;
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

		int buttonY = topPos + 22;

		signatureField = addRenderableWidget(new EditBox(font, leftPos + 58, topPos + 8, 42, 12, Component.empty()));
		signatureField.setValue(leftPaddedSignature());
		signatureField.setFilter(s -> s.matches("\\d{0,5}"));
		signatureField.setMaxLength(5);
		signatureField.setEditable(isOwner);
		signatureField.setResponder(this::changeSignatureFromField);

		minusThree = addRenderableWidget(Button.builder(Component.literal("---"), b -> changeSignature(signature - 100)).bounds(leftPos + 8, buttonY, 24, 13).build());
		minusTwo = addRenderableWidget(Button.builder(Component.literal("--"), b -> changeSignature(signature - 10)).bounds(leftPos + 34, buttonY, 18, 13).build());
		minusOne = addRenderableWidget(Button.builder(Component.literal("-"), b -> changeSignature(signature - 1)).bounds(leftPos + 54, buttonY, 12, 13).build());
		reset = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 68, buttonY, 13, 13, RESET_SPRITE, RESET_INACTIVE_SPRITE, 1, 1, 11, 11, b -> changeSignature(previousSignature)));
		plusOne = addRenderableWidget(Button.builder(Component.literal("+"), b -> changeSignature(signature + 1)).bounds(leftPos + 83, buttonY, 12, 13).build());
		plusTwo = addRenderableWidget(Button.builder(Component.literal("++"), b -> changeSignature(signature + 10)).bounds(leftPos + 97, buttonY, 18, 13).build());
		plusThree = addRenderableWidget(Button.builder(Component.literal("+++"), b -> changeSignature(signature + 100)).bounds(leftPos + 117, buttonY, 24, 13).build());
		randomize = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 143, buttonY, 13, 13, RANDOM_SPRITE, RANDOM_INACTIVE_SPRITE, 1, 1, 11, 11, b -> changeSignature(RANDOM.nextInt(MAX_SIGNATURE))));

		for (Button b : new Button[] {
				minusThree, minusTwo, minusOne, reset, plusOne, plusTwo, plusThree, randomize
		}) {
			b.active = isOwner;
		}

		for (int i = 0; i < 5; i++) {
			int index = i;
			int y = topPos + 56 + i * 15;

			levelBoxes[i] = addRenderableWidget(Button.builder(acceptedLevels[i] ? CHECK : CROSS, b -> {
				acceptedLevels[index] = !acceptedLevels[index];
				b.setMessage(acceptedLevels[index] ? CHECK : CROSS);
			}).bounds(leftPos + 100, y, 15, 15).build());
			levelBoxes[i].active = isOwner;
		}

		usableByField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 40, 78, 14, Component.empty()));
		usableByField.setHint(Utils.localize("gui.securitycraft:keycard_reader.usable_by.hint"));
		usableByField.setMaxLength(16);
		usableByField.setEditable(isOwner);

		linkButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:keycard_reader.link"), b -> {
			ClientPlayNetworking.send(SyncKeycardSettingsPayload.CHANNEL, new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, true, getUsableBy()).write());
		}).bounds(leftPos + 8, topPos + 128, 78, 18).build());
		linkButton.active = false;

		usesField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 108, 40, 14, Component.empty()));
		usesField.setFilter(s -> s.matches("\\d*"));
		usesField.setMaxLength(4);

		setUsesButton = addRenderableWidget(new ActiveBasedTextureButton(leftPos + 50, topPos + 108, 16, 16, RETURN_SPRITE, RETURN_INACTIVE_SPRITE, 2, 2, 12, 12, b -> {
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

		Component keycardLevels = Utils.localize("gui.securitycraft:keycard_reader.keycard_levels");

		guiGraphics.drawString(font, keycardLevels, 168 - font.width(keycardLevels), 44, 4210752, false);

		for (int i = 0; i < 5; i++) {
			guiGraphics.drawString(font, "" + (i + 1), 90, 60 + i * 15, 4210752, false);
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
