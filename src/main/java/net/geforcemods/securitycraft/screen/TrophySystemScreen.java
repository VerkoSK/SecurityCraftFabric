package net.geforcemods.securitycraft.screen;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.TrophySystemBlockEntity;
import net.geforcemods.securitycraft.inventory.TrophySystemMenu;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.screen.components.ToggleScrollList;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;

public class TrophySystemScreen extends AbstractContainerScreen<TrophySystemMenu> {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(SecurityCraft.MODID, "textures/gui/container/trophy_system.png");
	private final Component smartModuleTooltip;
	private TrophySystemBlockEntity be;
	private ToggleScrollList<EntityType<?>> scrollList;

	public TrophySystemScreen(TrophySystemMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.be = menu.be;
		imageWidth = 176;
		imageHeight = 256;
		inventoryLabelY = imageHeight - 94;
		smartModuleTooltip = Utils.localize(be.isModuleEnabled(ModuleType.SMART) ? "gui.securitycraft:trophy_system.toggle" : "gui.securitycraft:trophy_system.moduleRequired");
	}

	@Override
	protected void init() {
		super.init();
		titleLabelX = imageWidth / 2 - font.width(title) / 2;
		scrollList = addRenderableWidget(new ToggleScrollList<>(be, be.isModuleEnabled(ModuleType.SMART), false, minecraft, imageWidth - 40, 106, topPos + 30, leftPos + 20));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderLabels(guiGraphics, mouseX, mouseY);

		if (font.width(smartModuleTooltip) > imageWidth - 12) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().scale(0.8F, 0.8F, 0.8F);
			guiGraphics.drawWordWrap(font, smartModuleTooltip, (int) ((imageWidth / 2 - (imageWidth - 20) / 2) * 1.25F), (int) (142 * 1.25F), (int) ((imageWidth - 20) * 1.25F), 0x404040);
			guiGraphics.pose().popPose();
		}
		else
			guiGraphics.drawString(font, smartModuleTooltip, imageWidth / 2 - font.width(smartModuleTooltip) / 2, 145, 0x404040, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollList != null && scrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
}
