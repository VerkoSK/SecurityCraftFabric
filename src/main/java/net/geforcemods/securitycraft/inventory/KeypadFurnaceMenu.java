package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.AbstractKeypadFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class KeypadFurnaceMenu extends AbstractKeypadFurnaceMenu {
	public KeypadFurnaceMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
		this(windowId, inventory.player.level(), buf.readBlockPos(), inventory);
	}

	public KeypadFurnaceMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		this(windowId, inventory, (AbstractKeypadFurnaceBlockEntity) level.getBlockEntity(pos));
	}

	public KeypadFurnaceMenu(int windowId, Inventory inventory, AbstractKeypadFurnaceBlockEntity be) {
		super(SCContent.KEYPAD_FURNACE_MENU, RecipeType.SMELTING, RecipeBookType.FURNACE, windowId, inventory, be);
	}
}
