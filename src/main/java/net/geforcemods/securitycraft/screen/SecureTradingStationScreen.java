package net.geforcemods.securitycraft.screen;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.SecureTradingStationBlockEntity;
import net.geforcemods.securitycraft.blocks.SecureTradingStationBlock;
import net.geforcemods.securitycraft.inventory.SecureTradingStationMenu;
import net.geforcemods.securitycraft.network.RequestSecureTradingStationTransactionsPayload;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SecureTradingStationScreen extends AbstractContainerScreen<SecureTradingStationMenu> {
	private static final Identifier GUI_TEXTURE = SCContent.id("textures/gui/container/secure_trading_station.png");
	private static final Identifier GUI_TEXTURE_OWNER = SCContent.id("textures/gui/container/secure_trading_station_owner.png");
	private static final Identifier GUI_TEXTURE_STORAGE = SCContent.id("textures/gui/container/secure_trading_station_storage.png");
	private static final Identifier GUI_TEXTURE_OWNER_STORAGE = SCContent.id("textures/gui/container/secure_trading_station_owner_storage.png");
	private static final Identifier WORLD_SELECTION_ICONS = Identifier.withDefaultNamespace("textures/gui/world_selection.png");
	private final Component paymentText = Utils.localize("gui.securitycraft:secure_trading_station.payment");
	private final Component rewardText = Utils.localize("gui.securitycraft:secure_trading_station.reward");
	private final Component rewardActivateText = Utils.localize("gui.securitycraft:secure_trading_station.reward_activate_redstone");
	private final Component rewardToggleText = Utils.localize("gui.securitycraft:secure_trading_station.reward_toggle_redstone");
	private final Component storedItemsText = Utils.localize("gui.securitycraft:secure_trading_station.reward_items");
	private final Component payButtonText = Utils.localize("gui.securitycraft:secure_trading_station.pay_button");
	private final Component transactionAmountBoxTooltip = Utils.localize("gui.securitycraft:secure_trading_station.transaction_amount_tooltip");
	private final Component noRewardText = Utils.localize("gui.securitycraft:secure_trading_station.no_reward");
	private final Component confirmText = Utils.localize("gui.securitycraft:secure_trading_station.confirm");
	private final Component allowedText = Utils.localize("gui.securitycraft:secure_trading_station.allowed");
	private final SecureTradingStationBlockEntity be;
	private final boolean isOwner;
	private final boolean skipPaymentCheck;
	private final boolean storageVisible;
	private Button payButton;
	private EditBox transactionAmountBox;
	private int requestedTransactions;

	public SecureTradingStationScreen(SecureTradingStationMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, calculateImageHeight(menu));

		be = menu.be;
		isOwner = be.isOwnedBy(playerInventory.player);
		skipPaymentCheck = isOwner || be.isAllowed(playerInventory.player);
		storageVisible = be.hasRewardReferenceStacks() || menu.withStorageAccess;
		inventoryLabelY = imageHeight - 94;
	}

	private static int calculateImageHeight(SecureTradingStationMenu menu) {
		boolean storage = menu.be.hasRewardReferenceStacks() || menu.withStorageAccess;
		return storage ? (menu.withStorageAccess ? 249 : 208) : 184;
	}

	@Override
	protected void init() {
		super.init();

		payButton = addRenderableWidget(Button.builder(payButtonText, this::sendTransactionRequest).bounds(leftPos + 112, topPos + 42, 50, 16).build());

		if ((be.hasPaymentReferenceStacks() && !skipPaymentCheck) || (be.getSignalLength() != 0 && be.getBlockState().getValue(SecureTradingStationBlock.POWERED)))
			payButton.active = false; //Preliminary checks for empty container

		if (storageVisible) {
			transactionAmountBox = addRenderableWidget(new EditBox(font, leftPos + 118, topPos + 20, 26, 16, Component.empty()));
			transactionAmountBox.setResponder(s -> {
				String filtered = s.replaceAll("[^0-9]", "");
				if (!filtered.equals(s))
					transactionAmountBox.setValue(filtered);
			});
			transactionAmountBox.setMaxLength(3);
			transactionAmountBox.setHint(Component.literal("1").withStyle(ChatFormatting.GRAY));
			transactionAmountBox.setTooltip(Tooltip.create(transactionAmountBoxTooltip));
			transactionAmountBox.setResponder(s -> requestedTransactions = s.isEmpty() ? 1 : Integer.parseInt(s));
			requestedTransactions = 1;
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		if (be.getSignalLength() != 0 && be.getBlockState().getValue(SecureTradingStationBlock.POWERED))
			payButton.active = false;
		else
			payButton.active = getTransactionsOnConfirmation() > 0;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		Identifier backgroundTexture;

		if (isOwner)
			backgroundTexture = storageVisible ? GUI_TEXTURE_OWNER_STORAGE : GUI_TEXTURE_OWNER;
		else
			backgroundTexture = storageVisible ? GUI_TEXTURE_STORAGE : GUI_TEXTURE;

		extractor.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);

		if (be.rewardLimitedTransactions < 0) {
			if (mouseX >= leftPos + 112 && mouseX <= leftPos + 161 && mouseY >= topPos + 43 && mouseY <= topPos + 58) {
				Component warning;

				if (be.rewardLimitedTransactions == 0)
					warning = noRewardText;
				else
					warning = Utils.localize("gui.securitycraft:secure_trading_station.not_enough_reward", be.rewardLimitedTransactions);

				extractor.setComponentTooltipForNextFrame(font, List.of(warning), mouseX, mouseY);
			}
		}

		if (payButton.active && mouseX >= leftPos + 112 && mouseX <= leftPos + 161 && mouseY >= topPos + 43 && mouseY <= topPos + 58) {
			List<Component> buttonTooltip = new ArrayList<>();
			int transactions = getTransactionsOnConfirmation();

			if (transactions == 1)
				buttonTooltip.add(confirmText);
			else
				buttonTooltip.add(Utils.localize("gui.securitycraft:secure_trading_station.confirm_multiple", transactions));

			if (skipPaymentCheck)
				buttonTooltip.add(allowedText);

			extractor.setComponentTooltipForNextFrame(font, buttonTooltip, mouseX, mouseY);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
		super.extractLabels(extractor, mouseX, mouseY);
		extractor.text(font, paymentText, 12, 24, 0xFF404040, false);
		extractor.text(font, rewardText, 12, 64, 0xFF404040, false);
		extractor.text(font, be.getSignalLength() == 0 ? rewardToggleText : rewardActivateText, 12, 76, 0xFF404040, false);

		if (storageVisible)
			extractor.text(font, storedItemsText, 12, 96, 0xFF404040, false);
	}

	private void sendTransactionRequest(Button button) {
		ClientPlayNetworking.send(new RequestSecureTradingStationTransactionsPayload(be.getBlockPos(), getTransactionsOnConfirmation()));

		if (transactionAmountBox != null)
			transactionAmountBox.setValue("");
	}

	private int getTransactionsOnConfirmation() {
		int requestedTransactions = Math.max(1, this.requestedTransactions);

		return skipPaymentCheck ? requestedTransactions : Math.min(menu.paymentLimitedTransactions, requestedTransactions);
	}
}
