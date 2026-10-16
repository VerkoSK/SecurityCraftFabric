package net.geforcemods.securitycraft.screen;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.inventory.DisguiseModuleMenu;
import net.geforcemods.securitycraft.network.SetStateOnDisguiseModulePayload;
import net.geforcemods.securitycraft.screen.components.StateSelector;
import net.geforcemods.securitycraft.util.IHasExtraAreas;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DisguiseModuleScreen extends AbstractContainerScreen<DisguiseModuleMenu> implements IHasExtraAreas {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/customize1.png");
	private StateSelector stateSelector;

	public DisguiseModuleScreen(DisguiseModuleMenu menu, Inventory inv, Component title) {
		super(menu, inv, Utils.localize("item.securitycraft.disguise_module"));
	}

	@Override
	protected void init() {
		super.init();

		leftPos += 90;
		stateSelector = addRenderableWidget(new StateSelector(menu, titleLabelX, leftPos - 190, topPos + 7, 0, 200, 10));
		stateSelector.init();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty())
			renderTooltip(guiGraphics, mouseX, mouseY);

		stateSelector.renderTooltips(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		renderBackground(guiGraphics);
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (stateSelector != null && stateSelector.mouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public void onClose() {
		super.onClose();

		if (stateSelector != null) {
			ItemStack module = menu.getInventory().getModule();
			CompoundTag moduleTag = module.getOrCreateTag();
			BlockState state = stateSelector.getState();
			StandingOrWallType standingOrWall = stateSelector.getStandingOrWallType();

			if (state.getBlock() == Blocks.AIR && moduleTag.contains("SavedState") && moduleTag.contains("StandingOrWall")) {
				moduleTag.remove("SavedState");
				moduleTag.remove("StandingOrWall");
			}
			else {
				moduleTag.put("SavedState", NbtUtils.writeBlockState(state));
				moduleTag.putInt("StandingOrWall", standingOrWall.ordinal());
			}

			ClientPlayNetworking.send(SetStateOnDisguiseModulePayload.CHANNEL, new SetStateOnDisguiseModulePayload(state, standingOrWall).write());
		}
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		if (stateSelector != null)
			return stateSelector.getGuiExtraAreas();
		else
			return List.of();
	}
}
