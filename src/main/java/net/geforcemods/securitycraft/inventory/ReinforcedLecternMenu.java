package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ReinforcedLecternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class ReinforcedLecternMenu extends LecternMenu {
	public final ReinforcedLecternBlockEntity be;

	/** Client-side constructor: reads the position sent by the block entity's screen-opening data. */
	public ReinforcedLecternMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
		this(id, (ReinforcedLecternBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
	}

	public ReinforcedLecternMenu(int id, Level level, BlockPos pos, Inventory inventory) {
		this(id, (ReinforcedLecternBlockEntity) level.getBlockEntity(pos));
	}

	public ReinforcedLecternMenu(int id, ReinforcedLecternBlockEntity be) {
		super(id, be.bookAccess, be.dataAccess);
		this.be = be;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		//while the respective buttons are removed in the screen clientside, the server should still prevent any attempts by unallowed clients at using their functionality
		if (!be.isOwnedBy(player) && (id == LecternMenu.BUTTON_TAKE_BOOK || be.isPageLocked() && (id == LecternMenu.BUTTON_PREV_PAGE || id == LecternMenu.BUTTON_NEXT_PAGE)))
			return false;

		return super.clickMenuButton(player, id);
	}

	@Override
	public MenuType<?> getType() {
		return SCContent.REINFORCED_LECTERN_MENU;
	}
}
