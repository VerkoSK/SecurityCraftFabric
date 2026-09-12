package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.KeycardHolderItem;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 6 keycard slots backed by a Keycard Holder's NBT, plus the player's inventory. */
public class KeycardHolderMenu extends AbstractContainerMenu {
	private final KeycardHolderContainer inventory;

	public KeycardHolderMenu(int windowId, Inventory playerInventory) {
		this(windowId, playerInventory, PlayerUtils.getItemStackFromAnyHand(playerInventory.player, SCContent.KEYCARD_HOLDER));
	}

	public KeycardHolderMenu(int windowId, Inventory playerInventory, ItemStack holder) {
		super(SCContent.KEYCARD_HOLDER_MENU, windowId);
		inventory = new KeycardHolderContainer(holder);
		inventory.setMenu(this);

		for (int i = 0; i < KeycardHolderItem.SLOTS; i++) {
			addSlot(new Slot(inventory, i, 26 + (i % 3) * 18, 20 + (i / 3) * 18));
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(playerInventory, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		int size = inventory.getContainerSize();

		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();

			slotStackCopy = slotStack.copy();

			if (index < size) {
				if (!moveItemStackTo(slotStack, size, 37, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(slotStack, slotStackCopy);
			}
			else if (!moveItemStackTo(slotStack, 0, size, false))
				return ItemStack.EMPTY;

			if (slotStack.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (slotStack.getCount() == slotStackCopy.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
			broadcastChanges();
		}

		return slotStackCopy;
	}

	@Override
	public void clicked(int slot, int dragType, ClickType clickType, Player player) {
		if (!(slot >= 0 && getSlot(slot) != null && getSlot(slot).getItem().getItem() == SCContent.KEYCARD_HOLDER))
			super.clicked(slot, dragType, clickType, player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		inventory.stopOpen(player);
	}
}
