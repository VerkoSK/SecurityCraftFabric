package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.screen.components.ToggleScrollList;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RiftStabilizerScreen extends Screen {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(SecurityCraft.MODID, "textures/gui/container/blank.png");
	private final Component smartModuleTooltip;
	private final int imageWidth = 176;
	private final int imageHeight = 166;
	private int leftPos;
	private int topPos;
	private RiftStabilizerBlockEntity be;
	private ToggleScrollList<TeleportationType> scrollList;

	public RiftStabilizerScreen(RiftStabilizerBlockEntity be) {
		super(be.getDisplayName());
		this.be = be;
		smartModuleTooltip = Utils.localize(be.isModuleEnabled(ModuleType.SMART) ? "gui.securitycraft:rift_stabilizer.toggle" : "gui.securitycraft:rift_stabilizer.moduleRequired");
	}

	@Override
	protected void init() {
		super.init();
		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;
		scrollList = addRenderableWidget(new ToggleScrollList<>(be, be.isModuleEnabled(ModuleType.SMART), be.isModuleEnabled(ModuleType.REDSTONE), minecraft, imageWidth - 40, 106, topPos + 20, leftPos + 20));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(font, title, width / 2 - font.width(title) / 2, topPos + 6, 4210752, false);

		if (font.width(smartModuleTooltip) > imageWidth - 12) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().scale(0.8F, 0.8F, 0.8F);
			guiGraphics.drawWordWrap(font, smartModuleTooltip, (int) ((leftPos + imageWidth / 2 - (imageWidth - 20) / 2) * 1.25F), (int) ((topPos + 132) * 1.25F), (int) ((imageWidth - 20) * 1.25F), 0x404040);
			guiGraphics.pose().popPose();
		}
		else
			guiGraphics.drawString(font, smartModuleTooltip, width / 2 - font.width(smartModuleTooltip) / 2, topPos + 135, 0x404040, false);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollList != null && scrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
