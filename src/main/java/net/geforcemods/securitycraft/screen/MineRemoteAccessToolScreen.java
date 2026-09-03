package net.geforcemods.securitycraft.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.components.BoundMines;
import net.geforcemods.securitycraft.misc.StillValid;
import net.geforcemods.securitycraft.network.RemoteControlMinePayload;
import net.geforcemods.securitycraft.network.RemoteControlMinePayload.Action;
import net.geforcemods.securitycraft.network.RemoveMineFromMRATPayload;
import net.geforcemods.securitycraft.screen.components.PictureButton;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * The mine remote access tool's screen. Adapted from the upstream class of the same name: bound mine positions are
 * read from the {@link BoundMines} data component instead of upstream's per-slot NBT tags.
 */
public class MineRemoteAccessToolScreen extends Screen implements StillValid {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/mrat.png");
	private static final int DEFUSE = 0, ACTIVATE = 1, DETONATE = 2, UNBIND = 3;
	private ItemStack mrat;
	private Button[][] guiButtons = new Button[6][4]; //6 mines, 4 actions (defuse, prime, detonate, unbind)
	private int xSize = 256, ySize = 184;
	private final Component notBound = Utils.localize("gui.securitycraft:mrat.notBound");
	private final Component[] lines = new Component[6];
	private final int[] lengths = new int[6];

	public MineRemoteAccessToolScreen(ItemStack item) {
		super(item.getHoverName());

		mrat = item;
	}

	@Override
	public void init() {
		super.init();

		int padding = 25;
		int y = 50;
		int id = 0;
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;

		for (int i = 0; i < 6; i++) {
			y += 25;

			// initialize buttons
			for (int j = 0; j < 4; j++) {
				int btnX = startX + j * padding + 154;
				int btnY = startY + y - 48;
				int mine = id / 4;
				int action = id % 4;

				id++;

				switch (j) {
					case DEFUSE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, new ItemStack(SCContent.WIRE_CUTTERS), b -> buttonClicked(mine, action));
						break;
					case ACTIVATE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, new ItemStack(Items.FLINT_AND_STEEL), b -> buttonClicked(mine, action));
						break;
					case DETONATE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, new ItemStack(Items.TNT), b -> buttonClicked(mine, action));
						break;
					case UNBIND:
						guiButtons[i][j] = Button.builder(Component.literal("X"), b -> buttonClicked(mine, action)).bounds(btnX, btnY, 20, 20).build();
						break;
					default:
						throw new IllegalArgumentException("Mine actions can only range from 0-3 (inclusive)");
				}

				guiButtons[i][j].active = false;
				addRenderableWidget(guiButtons[i][j]);
			}

			Level level = minecraft.level;
			BlockPos minePos = getMineCoordinates(i);
			boolean foundMine = false;

			if (minePos != null) {
				guiButtons[i][UNBIND].active = true;
				lines[i] = Utils.localize("gui.securitycraft:mrat.mineLocations", Utils.getFormattedCoordinates(minePos));

				if (level.isLoaded(minePos)) {
					Block block = level.getBlockState(minePos).getBlock();

					if (block instanceof IExplosive explosive && (!(level.getBlockEntity(minePos) instanceof IOwnable ownable) || ownable.isOwnedBy(minecraft.player))) {
						boolean active = explosive.isActive(level, minePos);
						boolean defusable = explosive.isDefusable();

						guiButtons[i][DEFUSE].active = active && defusable;
						guiButtons[i][ACTIVATE].active = !active && defusable;
						guiButtons[i][DETONATE].active = active;
						guiButtons[i][DEFUSE].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.defuse")));
						guiButtons[i][ACTIVATE].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.activate")));
						guiButtons[i][DETONATE].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.detonate")));
						guiButtons[i][UNBIND].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.unbind")));
						foundMine = true;
					}
				}

				if (!foundMine) {
					for (int j = 0; j < 3; j++) {
						guiButtons[i][j].active = false;
						guiButtons[i][j].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.outOfRange")));
					}
				}

				guiButtons[i][UNBIND].setTooltip(Tooltip.create(Utils.localize("gui.securitycraft:mrat.unbind")));
			}
			else
				lines[i] = notBound;

			lengths[i] = font.width(lines[i]);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, startX, startY, 0, 0, xSize, ySize, 256, 256);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(font, title, startX + xSize / 2 - font.width(title) / 2, startY + 6, 0xFF404040, false);

		for (int i = 0; i < 6; i++) {
			guiGraphics.drawString(font, lines[i], startX + xSize / 2 - lengths[i] + 25, startY + i * 25 + 33, 0xFF404040, false);
		}
	}

	private void buttonClicked(int mine, int action) {
		BlockPos pos = getMineCoordinates(mine);

		if (pos != null) {
			switch (action) {
				case DEFUSE:
					((IExplosive) Minecraft.getInstance().player.level().getBlockState(pos).getBlock()).defuseMine(Minecraft.getInstance().player.level(), pos);
					ClientPlayNetworking.send(new RemoteControlMinePayload(pos, Action.DEFUSE));
					guiButtons[mine][DEFUSE].active = false;
					guiButtons[mine][ACTIVATE].active = true;
					guiButtons[mine][DETONATE].active = false;
					break;
				case ACTIVATE:
					((IExplosive) Minecraft.getInstance().player.level().getBlockState(pos).getBlock()).activateMine(Minecraft.getInstance().player.level(), pos);
					ClientPlayNetworking.send(new RemoteControlMinePayload(pos, Action.ACTIVATE));
					guiButtons[mine][DEFUSE].active = true;
					guiButtons[mine][ACTIVATE].active = false;
					guiButtons[mine][DETONATE].active = true;
					break;
				case DETONATE:
					ClientPlayNetworking.send(new RemoteControlMinePayload(pos, Action.DETONATE));
					removeFromToolAndUpdate(mine);

					for (int i = 0; i < 4; i++) {
						guiButtons[mine][i].active = false;
					}

					break;
				case UNBIND:
					removeFromToolAndUpdate(mine);

					for (int i = 0; i < 4; i++) {
						guiButtons[mine][i].active = false;
					}

					break;
				default:
					throw new IllegalArgumentException("Mine actions can only range from 0-3 (inclusive)");
			}
		}
	}

	/**
	 * @param mine 0 based
	 */
	private BlockPos getMineCoordinates(int mine) {
		if (mrat.getItem() != SCContent.MINE_REMOTE_ACCESS_TOOL)
			return null;

		BoundMines mines = mrat.get(SCContent.BOUND_MINES);

		return mines == null ? null : mines.positions().get(mine);
	}

	/**
	 * @param mine 0 based
	 */
	private void removeFromToolAndUpdate(int mine) {
		BoundMines mines = mrat.get(SCContent.BOUND_MINES);

		if (mines == null)
			return;

		mrat.set(SCContent.BOUND_MINES, mines.withoutSlot(mine));
		ClientPlayNetworking.send(new RemoveMineFromMRATPayload(mine));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean stillValid(Player player) {
		return !PlayerUtils.getItemStackFromAnyHand(player, mrat.getItem()).isEmpty();
	}
}
