package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.entity.sentry.Sentry.SentryMode;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.network.RemoveSentryFromSRATPayload;
import net.geforcemods.securitycraft.network.SetSentryModePayload;
import net.geforcemods.securitycraft.screen.components.PictureButton;
import net.geforcemods.securitycraft.screen.components.TextHoverChecker;
import net.geforcemods.securitycraft.screen.components.TogglePictureButton;
import net.geforcemods.securitycraft.util.IHasExtraAreas;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SentryRemoteAccessToolScreen extends Screen implements IHasExtraAreas {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SecurityCraft.MODID, "textures/gui/container/srat.png");
	private static final ResourceLocation SENTRY_ICONS = new ResourceLocation(SecurityCraft.MODID, "textures/gui/container/sentry_icons.png");
	private final Component modifyAll = Utils.localize("gui.securitycraft:srat.modifyAll");
	private ItemStack srat;
	private Button[][] guiButtons = new Button[12][4]; // 12 buttons, 4 actions (mode, targets, functionality, unbind)
	private Button[] guiButtonsGlobal = new Button[3];
	private static final int MODE = 0, TARGETS = 1, FUNCTIONALITY = 2, UNBIND = 3;
	private int xSize = 440, ySize = 215;
	private List<TextHoverChecker> hoverCheckers = new ArrayList<>();
	private final Component notBound = Utils.localize("gui.securitycraft:srat.notBound");
	private final Component[] lines = new Component[12];
	private final int[] lengths = new int[12];

	public SentryRemoteAccessToolScreen(ItemStack item) {
		super(Component.translatable(item.getDescriptionId()));

		srat = item;
	}

	@Override
	public void init() {
		super.init();

		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		int paddingX = 22;
		int paddingY = 50;
		int[] coords = null;
		int id = 0;
		boolean foundSentry = false;
		hoverCheckers.clear();

		for (int i = 0; i < 12; i++) {
			int x = (i / 6) * xSize / 2; //first 6 sentries in the left column, second 6 in the right column
			int y = ((i % 6) + 1) * 25 + paddingY;
			int btnY = startY + y - 48;
			coords = getSentryCoordinates(i);

			//initialize buttons
			for (int j = 0; j < 4; j++) {
				int btnX = startX + j * paddingX + 147 + x;

				switch (j) {
					case MODE:
						guiButtons[i][j] = new TogglePictureButton(btnX, btnY, 20, 20, SENTRY_ICONS, new int[] {
								0, 0, 0
						}, new int[] {
								0, 16, 32
						}, 2, 3, this::actionPerformed);
						guiButtons[i][j].active = false;
						break;
					case TARGETS:
						guiButtons[i][j] = new TogglePictureButton(btnX, btnY, 20, 20, SENTRY_ICONS, new int[] {
								16, 16, 16
						}, new int[] {
								0, 16, 32
						}, 2, 3, this::actionPerformed);
						guiButtons[i][j].active = false;
						break;
					case FUNCTIONALITY:
						guiButtons[i][j] = new TogglePictureButton(btnX, btnY, 20, 20, SENTRY_ICONS, new int[] {
								32, 32, 32
						}, new int[] {
								0, 16, 32
						}, 2, 3, this::actionPerformed);
						guiButtons[i][j].active = false;
						break;
					case UNBIND:
						guiButtons[i][j] = new Button(btnX, btnY, 20, 20, Component.literal("X"), this::unbindingAction, Button.DEFAULT_NARRATION);
						guiButtons[i][j].active = false;
						break;
				}

				addRenderableWidget(guiButtons[i][j]);
			}

			BlockPos sentryPos = new BlockPos(coords[0], coords[1], coords[2]);

			if (!(coords[0] == 0 && coords[1] == 0 && coords[2] == 0)) {
				String nameKey = "sentry" + (i + 1) + "_name";
				Component sentryName = null;

				if (srat.hasTag() && srat.getTag().contains(nameKey))
					sentryName = Component.literal(srat.getTag().getString(nameKey));

				guiButtons[i][UNBIND].active = true;

				if (Minecraft.getInstance().player.level().isLoaded(sentryPos)) {
					List<Sentry> sentries = Minecraft.getInstance().player.level().getEntitiesOfClass(Sentry.class, new AABB(sentryPos));

					if (!sentries.isEmpty()) {
						Sentry sentry = sentries.get(0);
						SentryMode mode = sentry.getMode();

						if (sentryName == null && sentry.hasCustomName())
							sentryName = sentry.getCustomName();

						guiButtons[i][MODE].active = true;
						guiButtons[i][TARGETS].active = mode != SentryMode.IDLE;
						guiButtons[i][FUNCTIONALITY].active = true;
						((TogglePictureButton) guiButtons[i][MODE]).setCurrentIndex(mode.ordinal() / 3);
						((TogglePictureButton) guiButtons[i][TARGETS]).setCurrentIndex(mode.ordinal() % 3);
						((TogglePictureButton) guiButtons[i][FUNCTIONALITY]).setCurrentIndex(mode == SentryMode.IDLE ? 1 : (mode.isCamouflage() ? 0 : 2));
						updateButtonTooltip((TogglePictureButton) guiButtons[i][MODE], (TogglePictureButton) guiButtons[i][TARGETS], (TogglePictureButton) guiButtons[i][FUNCTIONALITY]);
						foundSentry = true;
					}
				}

				if (sentryName != null) {
					int nameWidth = font.width(sentryName);

					lines[i] = sentryName;
					lengths[i] = nameWidth;
					hoverCheckers.add(new TextHoverChecker(btnY, btnY + 20, startX + x + 19, startX + x + 19 + Math.min(nameWidth, 120), Utils.getFormattedCoordinates(sentryPos)));
				}
				else {
					lines[i] = Utils.getFormattedCoordinates(sentryPos);
					lengths[i] = font.width(lines[i]);
				}

				if (!foundSentry) {
					for (int j = 0; j < 3; j++) {
						guiButtons[i][j].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:srat.outOfRange")));
					}
				}
			}
			else {
				lines[i] = notBound;
				lengths[i] = font.width(lines[i]);
			}
		}

		//Add buttons for global operation (all sentries)
		guiButtonsGlobal[0] = new TogglePictureButton(startX + 260, startY + 188, 20, 20, SENTRY_ICONS, new int[] {
				0, 0, 0
		}, new int[] {
				0, 16, 32
		}, 2, 3, this::globalActionPerformed);
		guiButtonsGlobal[1] = new TogglePictureButton(startX + 22 + 260, startY + 188, 20, 20, SENTRY_ICONS, new int[] {
				16, 16, 16
		}, new int[] {
				0, 16, 32
		}, 2, 3, this::globalActionPerformed);
		guiButtonsGlobal[2] = new TogglePictureButton(startX + 44 + 260, startY + 188, 20, 20, SENTRY_ICONS, new int[] {
				32, 32, 32
		}, new int[] {
				0, 16, 32
		}, 2, 3, this::globalActionPerformed);

		for (int j = 0; j < 3; j++) {
			guiButtonsGlobal[j].active = foundSentry;
			addRenderableWidget(guiButtonsGlobal[j]);
		}

		updateButtonTooltip((TogglePictureButton) guiButtonsGlobal[0], (TogglePictureButton) guiButtonsGlobal[1], (TogglePictureButton) guiButtonsGlobal[2]);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;

		renderBackground(guiGraphics);
		guiGraphics.blit(TEXTURE, startX, startY, 0, 0, xSize, ySize, 512, 256);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(font, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.getDescriptionId()), startX + 5, startY - 25 + 13, 0xFF0000);

		for (int i = 0; i < 12; i++) {
			int x = (i / 6) * xSize / 2; //first 6 sentries in the left column, second 6 in the right column
			int y = ((i % 6) + 1) * 25 + 50;

			guiGraphics.drawString(font, lines[i], startX + x + 70 - lengths[i] / 2, startY + y - 42, 4210752, false);
		}

		guiGraphics.drawString(font, modifyAll, startX + xSize / 2 - font.width(modifyAll) + 25, startY + 194, 4210752, false);

		for (TextHoverChecker chc : hoverCheckers) {
			if (chc != null && chc.checkHover(mouseX, mouseY)) {
				guiGraphics.renderTooltip(font, chc.getLines(), java.util.Optional.empty(), mouseX, mouseY);
				break;
			}
		}
	}

	/**
	 * Change the sentry mode after a button listening to a single sentry was clicked
	 */
	protected void actionPerformed(Button button) {
		int sentry = 0;
		int buttonType = 0;

		for (int i = 0; i < guiButtons.length; i++) {
			for (int j = 0; j < guiButtons[i].length; j++) {
				if (guiButtons[i][j] == button) {
					sentry = i;
					buttonType = j;
					break;
				}
			}
		}

		switch (buttonType) {
			case MODE:
				int modeIndex = ((TogglePictureButton) guiButtons[sentry][MODE]).getCurrentIndex();

				if (modeIndex == 2)
					((TogglePictureButton) guiButtons[sentry][FUNCTIONALITY]).setCurrentIndex(1);
				else if (((TogglePictureButton) guiButtons[sentry][FUNCTIONALITY]).getCurrentIndex() == 1)
					((TogglePictureButton) guiButtons[sentry][FUNCTIONALITY]).setCurrentIndex(modeIndex == 0 ? 2 : 0);

				guiButtons[sentry][TARGETS].active = modeIndex != 2;
				sendUpdates(new SingleSentryIndex(sentry));
				break;
			case TARGETS:
				sendUpdates(new SingleSentryIndex(sentry));
				break;
			case FUNCTIONALITY:
				int functionalityIndex = ((TogglePictureButton) guiButtons[sentry][FUNCTIONALITY]).getCurrentIndex();

				((TogglePictureButton) guiButtons[sentry][MODE]).setCurrentIndex(functionalityIndex == 0 ? 1 : (functionalityIndex == 1 ? 2 : 0));
				guiButtons[sentry][TARGETS].active = functionalityIndex != 1;
				sendUpdates(new SingleSentryIndex(sentry));
				break;
		}
	}

	/**
	 * Change the sentry mode after a global button (all sentries) was clicked
	 */
	protected void globalActionPerformed(Button button) {
		int buttonType;

		if (button == guiButtonsGlobal[MODE])
			buttonType = MODE;
		else if (button == guiButtonsGlobal[TARGETS])
			buttonType = TARGETS;
		else if (button == guiButtonsGlobal[FUNCTIONALITY])
			buttonType = FUNCTIONALITY;
		else
			return;

		for (int i = 0; i < guiButtons.length; i++) {
			TogglePictureButton modeButton = (TogglePictureButton) guiButtons[i][MODE];

			if (!Minecraft.getInstance().level.getEntitiesOfClass(Sentry.class, new AABB(BlockPos.of(BlockPos.asLong(getSentryCoordinates(i)[0], getSentryCoordinates(i)[1], getSentryCoordinates(i)[2])))).isEmpty()) {
				TogglePictureButton targetsButton = (TogglePictureButton) guiButtons[i][TARGETS];
				TogglePictureButton functionalityButton = (TogglePictureButton) guiButtons[i][FUNCTIONALITY];

				if (buttonType == MODE) {
					int modeIndex = ((TogglePictureButton) guiButtonsGlobal[MODE]).getCurrentIndex();

					modeButton.setCurrentIndex(modeIndex);

					if (modeIndex == 2)
						functionalityButton.setCurrentIndex(1);
					else if (functionalityButton.getCurrentIndex() == 1)
						functionalityButton.setCurrentIndex(modeIndex == 0 ? 2 : 0);

					guiButtonsGlobal[FUNCTIONALITY] = functionalityButton;
					guiButtonsGlobal[TARGETS].active = modeIndex != 2;
					targetsButton.active = modeIndex != 2;
				}
				else if (buttonType == TARGETS)
					targetsButton.setCurrentIndex(((TogglePictureButton) guiButtonsGlobal[TARGETS]).getCurrentIndex());
				else if (buttonType == FUNCTIONALITY) {
					int functionalityIndex = ((TogglePictureButton) guiButtonsGlobal[FUNCTIONALITY]).getCurrentIndex();

					functionalityButton.setCurrentIndex(functionalityIndex);
					modeButton.setCurrentIndex(functionalityIndex == 0 ? 1 : (functionalityIndex == 1 ? 2 : 0));
					guiButtonsGlobal[MODE] = modeButton;
					guiButtonsGlobal[TARGETS].active = functionalityIndex != 1;
					targetsButton.active = functionalityIndex != 1;
				}
			}
		}

		sendUpdates(new GlobalSentryIndex());
	}

	/**
	 * Unbinds a sentry from the SRAT after its unbind button was clicked
	 */
	protected void unbindingAction(Button button) {
		int sentry = 0;

		for (int i = 0; i < 12; i++) {
			if (guiButtons[i][UNBIND] == button) {
				sentry = i;
				break;
			}
		}

		int[] coords = getSentryCoordinates(sentry);

		removeTagFromToolAndUpdate(srat, coords[0], coords[1], coords[2]);

		for (int i = 0; i < 4; i++) {
			guiButtons[sentry][i].active = false;
			guiButtons[sentry][i].setTooltip(null);
		}

		for (int i = 0; i < guiButtons.length; i++) {
			if (guiButtons[i][UNBIND].active)
				return;
		}

		for (int i = 0; i < 3; i++) {
			guiButtonsGlobal[i].active = false;
			guiButtonsGlobal[i].setTooltip(null);
		}
	}

	/**
	 * @param sentry The zero-based index of the sentry whose coordinates to get
	 */
	private int[] getSentryCoordinates(int sentry) {
		sentry++; // sentry indices are 1-based in the tag

		if (srat.getItem() == SCContent.REMOTE_ACCESS_SENTRY && srat.hasTag()) {
			int[] coords = srat.getTag().getIntArray("sentry" + sentry);

			if (coords.length == 3)
				return coords;
		}

		return new int[] {
				0, 0, 0
		};
	}

	/**
	 * Sends packets to the server and updates the client-side sentry mode and button tooltips
	 */
	private void sendUpdates(SentryIndex index) {
		Level level = Minecraft.getInstance().level;
		List<SetSentryModePayload.Info> infoToSend = new ArrayList<>();

		for (int i : index.get()) {
			int[] coords = getSentryCoordinates(i);

			if (!(coords[0] == 0 && coords[1] == 0 && coords[2] == 0)) {
				BlockPos pos = new BlockPos(coords[0], coords[1], coords[2]);
				List<Sentry> sentries = level.getEntitiesOfClass(Sentry.class, new AABB(pos));

				if (!sentries.isEmpty()) {
					TogglePictureButton modeBtn = (TogglePictureButton) guiButtons[i][MODE];
					TogglePictureButton targetsBtn = (TogglePictureButton) guiButtons[i][TARGETS];
					TogglePictureButton functionalityBtn = (TogglePictureButton) guiButtons[i][FUNCTIONALITY];
					int mode = getSelectedMode(modeBtn, targetsBtn, functionalityBtn);

					sentries.get(0).toggleMode(Minecraft.getInstance().player, mode, false);
					infoToSend.add(new SetSentryModePayload.Info(pos, mode));
					updateButtonTooltip(modeBtn, targetsBtn, functionalityBtn);
				}
			}
		}

		if (index instanceof GlobalSentryIndex)
			updateButtonTooltip((TogglePictureButton) guiButtonsGlobal[MODE], (TogglePictureButton) guiButtonsGlobal[TARGETS], (TogglePictureButton) guiButtonsGlobal[FUNCTIONALITY]);

		NetworkHandler.sendToServer(new SetSentryModePayload(infoToSend));
	}

	private void updateButtonTooltip(TogglePictureButton modeButton, TogglePictureButton targetsButton, TogglePictureButton functionalityButton) {
		int mode = modeButton.getCurrentIndex();
		int targets = targetsButton.getCurrentIndex();
		int functionality = functionalityButton.getCurrentIndex();

		modeButton.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:srat.mode" + (mode == 2 ? 3 : mode + 1))));

		if (mode != 2)
			targetsButton.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:srat.targets" + (targets + 1))));
		else
			targetsButton.setTooltip(null);

		functionalityButton.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:srat.functionality" + (functionality + 1))));
	}

	private int getSelectedMode(TogglePictureButton modeButton, TogglePictureButton targetsButton, TogglePictureButton functionalityButton) {
		int mode = modeButton.getCurrentIndex();
		int targets = targetsButton.getCurrentIndex();
		int functionality = functionalityButton.getCurrentIndex();

		if (functionality == 1)
			return mode == 1 ? 7 : 6;
		else
			return Math.min(mode, 1) * 3 + targets;
	}

	private void removeTagFromToolAndUpdate(ItemStack stack, int x, int y, int z) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length == 3 && coords[0] == x && coords[1] == y && coords[2] == z) {
				stack.getTag().remove("sentry" + i);
				stack.getTag().remove("sentry" + i + "_name");
				NetworkHandler.sendToServer(new RemoveSentryFromSRATPayload(i));
				lines[i - 1] = notBound;
				lengths[i - 1] = font.width(lines[i - 1]);
				return;
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;

		return List.of(new Rect2i(startX, startY, xSize, ySize));
	}

	private abstract static class SentryIndex {
		public abstract List<Integer> get();
	}

	private static class SingleSentryIndex extends SentryIndex {
		private final int index;

		public SingleSentryIndex(int index) {
			this.index = index;
		}

		@Override
		public List<Integer> get() {
			return Arrays.asList(index);
		}
	}

	private static class GlobalSentryIndex extends SentryIndex {
		private static final List<Integer> INDICES = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

		@Override
		public List<Integer> get() {
			return INDICES;
		}
	}
}
