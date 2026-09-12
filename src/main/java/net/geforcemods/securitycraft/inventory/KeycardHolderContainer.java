package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.items.KeycardHolderItem;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Backs a {@link KeycardHolderMenu} with the keycards saved in the holder stack's NBT. */
public class KeycardHolderContainer implements Container {
	private final ItemStack holder;
	private final NonNullList<ItemStack> keycards;
	private KeycardHolderMenu menu;
	private boolean changed;

	public KeycardHolderContainer(ItemStack holder) {
		this.holder = holder;
		keycards = KeycardHolderItem.getContents(holder);
	}

	@Override
	public int getContainerSize() {
		return KeycardHolderItem.SLOTS;
	}

	@Override
	public ItemStack getItem(int index) {
		return keycards.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int size) {
		ItemStack stack = ContainerHelper.removeItem(keycards, index, size);

		if (!stack.isEmpty())
			setChanged();

		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack stack = keycards.get(index);

		keycards.set(index, ItemStack.EMPTY);
		setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		keycards.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > getMaxStackSize())
			stack.setCount(getMaxStackSize());

		setChanged();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public void setChanged() {
		changed = true;

		if (menu != null)
			menu.slotsChanged(this);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void startOpen(Player player) {}

	@Override
	public void stopOpen(Player player) {
		if (changed)
			KeycardHolderItem.setContents(holder, keycards);
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return stack.getItem() instanceof KeycardItem;
	}

	@Override
	public void clearContent() {
		keycards.clear();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : keycards) {
			if (!stack.isEmpty())
				return false;
		}

		return true;
	}

	public void setMenu(KeycardHolderMenu menu) {
		this.menu = menu;
	}
}
