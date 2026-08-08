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
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** The mine remote access tool's screen. 1:1 with the upstream class of the same name. */
public class MineRemoteAccessToolScreen extends Screen implements StillValid {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/mrat.png");
	private static final ResourceLocation INFO_BOOK_ICONS = SCContent.id("textures/gui/info_book_icons.png"); //for the explosion icon
	private static final int DEFUSE = 0, ACTIVATE = 1, DETONATE = 2, UNBIND = 3;
	private ItemStack mrat;
	private Button[][] guiButtons = new Button[6][4]; //6 mines, 4 actions (defuse, prime, detonate, unbind)
	private int xSize = 256, ySize = 184, leftPos, topPos;
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
		leftPos = (width - xSize) / 2;
		topPos = (height - ySize) / 2;

		int padding = 25;
		int y = 50;
		int id = 0;

		for (int i = 0; i < 6; i++) {
			y += 25;

			// initialize buttons
			for (int j = 0; j < 4; j++) {
				int btnX = leftPos + j * padding + 154;
				int btnY = topPos + y - 48;
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
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, INFO_BOOK_ICONS, 54, 1, 0, 1, 18, 18, 256, 256, b -> buttonClicked(mine, action));
						break;
					case UNBIND:
						//upstream uses Button's protected constructor (widened by a Forge AT); the vanilla builder is the Fabric-side equivalent
						guiButtons[i][j] = Button.builder(Component.literal("X"), b -> buttonClicked(mine, action)).bounds(btnX, btnY, 20, 20).build();
						break;
					default:
						throw new IllegalArgumentException("Mine actions can only range from 0-3 (inclusive)");
				}

				guiButtons[i][j].active = false;
				addRenderableWidget(guiButtons[i][j]);
			}

			GlobalPos globalPos = getMineCoordinates(i);
			boolean foundMine = false;

			if (globalPos != null) {
				Level level = minecraft.level;
				BlockPos minePos = globalPos.pos();

				guiButtons[i][UNBIND].active = true;
				//Upstream passes the BlockPos straight in, which only reads well because Forge keeps the mapped class
				//name; in a remapped Fabric jar BlockPos#toString prints "class_2338{x=..}", so format it like the
				//tool's chat messages do instead.
				lines[i] = Utils.localize("gui.securitycraft:mrat.mineLocations", Utils.getFormattedCoordinates(minePos));

				if (globalPos.dimension().equals(level.dimension()) && level.isLoaded(minePos)) {
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
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(font, title, leftPos + xSize / 2 - font.width(title) / 2, topPos + 6, 4210752, false);

		for (int i = 0; i < 6; i++) {
			guiGraphics.drawString(font, lines[i], leftPos + xSize / 2 - lengths[i] + 25, topPos + i * 25 + 33, 4210752, false);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(guiGraphics);
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, xSize, ySize);
	}

	private void buttonClicked(int mine, int action) {
		GlobalPos globalPos = getMineCoordinates(mine);

		if (globalPos != null) {
			BlockPos pos = globalPos.pos();

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
					removeMineFromToolAndUpdate(mrat, globalPos);

					for (int i = 0; i < 4; i++) {
						guiButtons[mine][i].active = false;
					}

					break;
				case UNBIND:
					removeMineFromToolAndUpdate(mrat, globalPos);

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
	private GlobalPos getMineCoordinates(int mine) {
		if (mrat.getItem() == SCContent.MINE_REMOTE_ACCESS_TOOL) {
			BoundMines positions = mrat.get(SCContent.BOUND_MINES);

			if (positions != null)
				return positions.get(mine);
		}

		return null;
	}

	private void removeMineFromToolAndUpdate(ItemStack stack, GlobalPos globalPos) {
		BoundMines positions = stack.get(SCContent.BOUND_MINES);

		if (positions != null && positions.remove(stack, globalPos))
			ClientPlayNetworking.send(new RemoveMineFromMRATPayload(globalPos));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		//upstream calls the Forge-added KeyMapping#isActiveAndMatches; vanilla's matches(int, int) is the equivalent (see LaserBlockScreen)
		if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean stillValid(Player player) {
		return !PlayerUtils.getItemStackFromAnyHand(player, mrat.getItem()).isEmpty();
	}
}
