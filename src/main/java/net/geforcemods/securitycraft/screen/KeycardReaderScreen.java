package net.geforcemods.securitycraft.screen;

import java.util.Optional;
import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.inventory.KeycardReaderMenu;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.network.SetKeycardUsesPayload;
import net.geforcemods.securitycraft.network.SyncKeycardSettingsPayload;
import net.geforcemods.securitycraft.screen.components.CallbackCheckbox;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The keycard reader/lock's programming screen: put an unlinked (or your own already-linked) keycard in the slot, pick
 * which levels it should open, optionally restrict it to one player's name, then Link. Simplified from upstream: no
 * per-digit signature stepper buttons - Randomize picks a new one, and it can also be typed directly.
 */
public class KeycardReaderScreen extends AbstractContainerScreen<KeycardReaderMenu> {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/keycard_reader.png");
	private final KeycardReaderBlockEntity be;
	private final boolean isOwner;
	private boolean[] acceptedLevels;
	private int signature;
	private CallbackCheckbox[] levelBoxes = new CallbackCheckbox[5];
	private EditBox signatureField, usableByField, usesField;
	private Button linkButton, setUsesButton;
	private boolean firstTick = true;

	public KeycardReaderScreen(KeycardReaderMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		be = menu.be;
		acceptedLevels = be.getAcceptedLevels().clone();
		signature = be.getSignature();
		isOwner = be.isOwnedBy(inv.player);
		imageWidth = 176;
		imageHeight = 249;
	}

	@Override
	public void init() {
		super.init();
		titleLabelX = imageWidth / 2 - font.width(title) / 2;

		for (int i = 0; i < 5; i++) {
			int index = i;

			levelBoxes[i] = addRenderableWidget(new CallbackCheckbox(leftPos + 100, topPos + 34 + i * 16, 12, 12, Component.literal("Level " + (i + 1)), acceptedLevels[i], selected -> acceptedLevels[index] = selected, 4210752));
			levelBoxes[i].active = isOwner;
		}

		signatureField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 22, 50, 12, Component.empty()));
		signatureField.setValue(String.format("%05d", signature));
		signatureField.setFilter(s -> s.matches("\\d{0,5}"));
		signatureField.setMaxLength(5);
		signatureField.setEditable(isOwner);

		Button randomize = addRenderableWidget(Button.builder(Component.translatable("gui.securitycraft:keycard_reader.randomize_signature"), b -> {
			signature = new Random().nextInt(100000);
			signatureField.setValue(String.format("%05d", signature));
		}).bounds(leftPos + 62, topPos + 21, 46, 14).build());

		randomize.active = isOwner;

		usableByField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 66, 90, 14, Component.empty()));
		usableByField.setHint(Utils.localize("gui.securitycraft:keycard_reader.usable_by.hint"));
		usableByField.setMaxLength(16);
		usableByField.setEditable(isOwner);

		linkButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:keycard_reader.link"), b -> {
			signature = parseSignature();
			ClientPlayNetworking.send(SyncKeycardSettingsPayload.CHANNEL, new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, signature, true, getUsableBy()).write());
		}).bounds(leftPos + 8, topPos + 106, 70, 20).build());
		linkButton.active = false;

		usesField = addRenderableWidget(new EditBox(font, leftPos + 90, topPos + 106, 40, 14, Component.empty()));
		usesField.setFilter(s -> s.matches("\\d*"));
		usesField.setMaxLength(4);

		setUsesButton = addRenderableWidget(Button.builder(Utils.localize("gui.securitycraft:keycard_reader.set_uses"), b -> {
			if (!usesField.getValue().isEmpty())
				ClientPlayNetworking.send(SetKeycardUsesPayload.CHANNEL, new SetKeycardUsesPayload(be.getBlockPos(), Integer.parseInt(usesField.getValue())).write());
		}).bounds(leftPos + 132, topPos + 106, 36, 20).build());
		setUsesButton.active = false;
	}

	private int parseSignature() {
		try {
			return signatureField.getValue().isEmpty() ? 0 : Integer.parseInt(signatureField.getValue());
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	private Optional<String> getUsableBy() {
		String value = usableByField.getValue();

		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, titleLabelX, 6, 4210752, false);
		guiGraphics.drawString(font, Utils.localize("gui.securitycraft:keycard_reader.signature"), 8, 12, 4210752, false);
		guiGraphics.drawString(font, Utils.localize("gui.securitycraft:keycard_reader.keycard_levels"), 8, 33, 4210752, false);
		guiGraphics.drawString(font, Utils.localize("gui.securitycraft:keycard_reader.usable_by"), 8, 56, 4210752, false);
		guiGraphics.drawString(font, Utils.localize("gui.securitycraft:keycard_reader.limited_uses"), 8, 96, 4210752, false);
		guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 93, 4210752, false);
	}

	@Override
	public void containerTick() {
		super.containerTick();

		ItemStack stack = menu.keycardSlot.getItem();
		boolean isEmpty = stack.isEmpty();
		boolean isLimited = !isEmpty && stack.getItem() instanceof KeycardItem keycard && keycard.isLimited();

		usesField.setEditable(isLimited);
		usesField.active = isLimited;

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
			ClientPlayNetworking.send(SyncKeycardSettingsPayload.CHANNEL, new SyncKeycardSettingsPayload(be.getBlockPos(), acceptedLevels, parseSignature(), false, getUsableBy()).write());
	}
}
