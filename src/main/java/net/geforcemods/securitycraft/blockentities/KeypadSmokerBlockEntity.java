package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.inventory.KeypadSmokerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class KeypadSmokerBlockEntity extends AbstractKeypadFurnaceBlockEntity {
	public KeypadSmokerBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_SMOKER_BLOCK_ENTITY, pos, state, RecipeType.SMOKING);
	}

	@Override
	protected int getBurnDuration(ItemStack fuel) {
		return super.getBurnDuration(fuel) / 2;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		return new KeypadSmokerMenu(windowId, inventory, this);
	}

	//BaseContainerBlockEntity demands this one; the vanilla furnace screen route never reaches it here, because the
	//block only ever opens the menu itself once the passcode has been accepted
	@Override
	protected AbstractContainerMenu createMenu(int windowId, Inventory inventory) {
		return new KeypadSmokerMenu(windowId, inventory, this);
	}
}
