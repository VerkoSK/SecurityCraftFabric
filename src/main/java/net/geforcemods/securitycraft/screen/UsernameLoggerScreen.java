package net.geforcemods.securitycraft.screen;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity;
import net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity.UsernameLoggerEntry;
import net.geforcemods.securitycraft.misc.StillValid;
import net.geforcemods.securitycraft.network.ClearLoggerServerPayload;
import net.geforcemods.securitycraft.screen.components.ScrollPanel;
import net.geforcemods.securitycraft.screen.components.SmallButton;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public class UsernameLoggerScreen extends Screen implements StillValid {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/blank.png");
	private final Component logged = Utils.localize("gui.securitycraft:logger.logged");
	private int imageWidth = 176;
	private int imageHeight = 166;
	private int leftPos;
	private int topPos;
	private UsernameLoggerBlockEntity be;
	private PlayerList playerList;

	public UsernameLoggerScreen(UsernameLoggerBlockEntity be) {
		super(be.getDisplayName());
		this.be = be;
	}

	@Override
	protected void init() {
		super.init();

		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;

		Button clearButton = addRenderableWidget(SmallButton.createWithX(leftPos + 4, topPos + 4, b -> {
			be.clearEntries();
			ClientPlayNetworking.send(new ClearLoggerServerPayload(be.getBlockPos()));
		}));

		clearButton.active = be.isOwnedBy(minecraft.player);
		clearButton.setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:editModule.clear")));
		playerList = addRenderableWidget(new PlayerList(minecraft, imageWidth - 24, imageHeight - 40, topPos + 20, leftPos + 12));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawString(font, logged, width / 2 - font.width(logged) / 2, topPos + 6, 4210752, false);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(guiGraphics);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (playerList != null && playerList.isMouseOver(mouseX, mouseY))
			return playerList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (playerList != null)
			playerList.mouseClicked(event, doubleClick);

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (playerList != null)
			playerList.mouseReleased(event);

		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(be, player);
	}

	class PlayerList extends ScrollPanel {
		private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		private static final int SLOT_HEIGHT = 12;

		public PlayerList(Minecraft client, int width, int height, int top, int left) {
			super(client, width, height, top, left);
		}

		@Override
		protected int getContentHeight() {
			int height = UsernameLoggerBlockEntity.LOGGER_LIST_SIZE * (font.lineHeight + 3);

			if (height < bottom - top - 4)
				height = bottom - top - 4;

			return height;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			super.render(guiGraphics, mouseX, mouseY, partialTicks);

			if (be.isOwnedBy(minecraft.player)) {
				int mouseListY = (int) (mouseY - top + scrollDistance - border);
				int slotIndex = mouseListY / SLOT_HEIGHT;

				if (mouseX >= left && mouseX < right - 6 && slotIndex >= 0 && mouseListY >= 0 && mouseY >= top && mouseY <= bottom) {
					UsernameLoggerEntry entry = be.getEntry(slotIndex);

					if (entry != null) {
						Component localized = Utils.localize("gui.securitycraft:logger.date", dateFormat.format(new Date(entry.timestamp())));

						guiGraphics.setTooltipForNextFrame(font, Component.literal(entry.uuid().toString()), mouseX, mouseY);
						guiGraphics.drawString(font, localized, leftPos + (imageWidth / 2 - font.width(localized) / 2), bottom + 5, 0xFF404040, false);
					}
				}
			}
		}

		@Override
		protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
			int baseY = top + border - (int) scrollDistance;
			int slotBuffer = SLOT_HEIGHT - 4;
			int mouseListY = (int) (mouseY - top + scrollDistance - border);
			int slotIndex = mouseListY / SLOT_HEIGHT;

			//highlight hovered slot
			if (mouseX >= left && mouseX <= right - 6 && slotIndex >= 0 && mouseListY >= 0 && mouseY >= top && mouseY <= bottom) {
				UsernameLoggerEntry entry = be.getEntry(slotIndex);

				if (entry != null) {
					int min = left;
					int max = entryRight - 6; //6 is the width of the scrollbar
					int slotTop = baseY + slotIndex * SLOT_HEIGHT;

					guiGraphics.fill(min, slotTop - 2, max, slotTop + slotBuffer + 2, 0xFF808080);
					guiGraphics.fill(min + 1, slotTop - 1, max - 1, slotTop + slotBuffer + 1, 0xFF000000);
				}
			}

			//draw entry strings
			for (int i = 0; i < be.getEntries().length; i++) {
				UsernameLoggerEntry entry = be.getEntry(i);

				if (entry != null)
					guiGraphics.drawString(font, entry.playerName(), left + width / 2 - font.width(entry.playerName()) / 2, relativeY + (SLOT_HEIGHT * i), 0xFFC6C6C6, false);
			}
		}

		@Override
		public NarrationPriority narrationPriority() {
			return NarrationPriority.NONE;
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}
}
