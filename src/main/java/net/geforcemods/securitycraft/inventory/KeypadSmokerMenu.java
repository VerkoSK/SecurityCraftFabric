package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.AbstractKeypadFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class KeypadSmokerMenu extends AbstractKeypadFurnaceMenu {
	public KeypadSmokerMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
		this(windowId, inventory.player.level(), buf.readBlockPos(), inventory);
	}

	public KeypadSmokerMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		this(windowId, inventory, (AbstractKeypadFurnaceBlockEntity) level.getBlockEntity(pos));
	}

	public KeypadSmokerMenu(int windowId, Inventory inventory, AbstractKeypadFurnaceBlockEntity be) {
		super(SCContent.KEYPAD_SMOKER_MENU, RecipeType.SMOKING, net.minecraft.world.item.crafting.RecipePropertySet.SMOKER_INPUT, RecipeBookType.SMOKER, windowId, inventory, be);
	}
}
