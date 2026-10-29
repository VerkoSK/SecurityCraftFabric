package net.geforcemods.securitycraft.screen.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.util.IToggleableEntries;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class ToggleScrollList<T> extends ScrollPanel {
	private static final int SLOT_HEIGHT = 12;
	private static final ResourceLocation BEACON_GUI = new ResourceLocation("textures/gui/container/beacon.png");
	private final Map<T, Component> typeNames = new HashMap<>();
	private final List<T> orderedFilterList;
	private final Minecraft mc;
	private final IToggleableEntries<T> be;
	private final boolean hasSmartModule;
	private final boolean hasRedstoneModule;
	private int listLength;

	public ToggleScrollList(IToggleableEntries<T> be, boolean hasSmartModule, boolean hasRedstoneModule, Minecraft client, int width, int height, int top, int left) {
		super(client, width, height, top, left);
		this.mc = client;
		this.be = be;
		this.hasSmartModule = hasSmartModule;
		this.hasRedstoneModule = hasRedstoneModule;
		orderedFilterList = new ArrayList<>(be.getFilters().keySet());
		orderedFilterList.sort((e1, e2) -> {
			// the default entry always goes at the bottom of the list
			if (e1 == be.getDefaultType())
				return 1;
			else if (e2 == be.getDefaultType())
				return -1;
			else
				return Utils.localize(e1.toString()).getString().compareTo(Utils.localize(e2.toString()).getString());
		});
		listLength = orderedFilterList.size();
	}

	@Override
	protected int getContentHeight() {
		int height = listLength * (mc.font.lineHeight + 3);

		if (height < bottom - top - 4)
			height = bottom - top - 4;

		return height;
	}

	@Override
	protected boolean clickPanel(double mouseX, double mouseY, int button) {
		if (hasSmartModule) {
			int slotIndex = (int) (mouseY + (border / 2)) / SLOT_HEIGHT;

			if (slotIndex >= 0 && slotIndex < listLength) {
				Minecraft mc = Minecraft.getInstance();

				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				be.setFilter(orderedFilterList.get(slotIndex), !be.getFilter(orderedFilterList.get(slotIndex)));
				return true;
			}
		}

		return false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int baseY = top + border - (int) scrollDistance;
		int mouseListY = (int) (mouseY - top + scrollDistance - (border / 2));
		int slotIndex = mouseListY / SLOT_HEIGHT;

		if (hasRedstoneModule && mouseX >= left && mouseX < right - 6 && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength && mouseY >= top && mouseY <= bottom) {
			Component comparatorOutput = Utils.localize("gui.securitycraft:toggleList.comparatorOutput", slotIndex + 1);
			int stringWidth = mc.font.width(comparatorOutput);
			int boxY = baseY + slotIndex * SLOT_HEIGHT;

			guiGraphics.renderTooltip(mc.font, Collections.singletonList(comparatorOutput), java.util.Optional.empty(), right - 8, boxY + SLOT_HEIGHT + 3);
		}
	}

	@Override
	protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tesselator, int mouseX, int mouseY) {
		Font font = Minecraft.getInstance().font;
		int baseY = top + border - (int) scrollDistance;
		int slotBuffer = SLOT_HEIGHT - 4;
		int mouseListY = (int) (mouseY - top + scrollDistance - (border / 2));
		int slotIndex = mouseListY / SLOT_HEIGHT;

		//highlight hovered slot
		if (hasSmartModule && mouseX >= left && mouseX <= right - 6 && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength && mouseY >= top && mouseY <= bottom) {
			int min = left;
			int max = entryRight - 6; //6 is the width of the scrollbar
			int slotTop = baseY + slotIndex * SLOT_HEIGHT;
			BufferBuilder bufferBuilder = tesselator.getBuilder();

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(min, slotTop + slotBuffer + 2, 0).uv(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			bufferBuilder.vertex(max, slotTop + slotBuffer + 2, 0).uv(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			bufferBuilder.vertex(max, slotTop - 2, 0).uv(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			bufferBuilder.vertex(min, slotTop - 2, 0).uv(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			bufferBuilder.vertex(min + 1, slotTop + slotBuffer + 1, 0).uv(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			bufferBuilder.vertex(max - 1, slotTop + slotBuffer + 1, 0).uv(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			bufferBuilder.vertex(max - 1, slotTop - 1, 0).uv(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			bufferBuilder.vertex(min + 1, slotTop - 1, 0).uv(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			BufferUploader.drawWithShader(bufferBuilder.end());
			RenderSystem.disableBlend();
		}

		int i = 0;

		//draw entry strings and indicators whether the filter is enabled
		for (T type : orderedFilterList) {
			Component name = typeNames.computeIfAbsent(type, t -> Utils.localize(t == be.getDefaultType() ? be.getDefaultTypeName() : t.toString()));
			int yStart = relativeY + (SLOT_HEIGHT * i);

			guiGraphics.drawString(font, name, left + width / 2 - 5 - font.width(name) / 2, yStart, 0xC6C6C6, false);
			guiGraphics.blit(BEACON_GUI, left, yStart - 3, 14, 14, be.getFilter(type) ? 88 : 110, 219, 21, 22, 256, 256);
			i++;
		}
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
