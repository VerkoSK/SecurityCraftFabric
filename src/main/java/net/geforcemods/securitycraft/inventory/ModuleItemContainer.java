package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.items.ModuleItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A 1-slot container backed by an item stack under the module's {@code SavedBlock} NBT key (the disguise block addon). */
public class ModuleItemContainer implements Container {
	public static final String SAVED_BLOCK_KEY = "SavedBlock";

	private final ItemStack module;
	private NonNullList<ItemStack> moduleInventory;
	private DisguiseModuleMenu menu;
	private boolean changed;

	public ModuleItemContainer(ItemStack moduleStack) {
		module = moduleStack;

		if (!(moduleStack.getItem() instanceof ModuleItem))
			return;

		moduleInventory = NonNullList.withSize(1, ItemStack.EMPTY);
		load();
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public ItemStack getItem(int index) {
		return moduleInventory.get(index);
	}

	public void load() {
		ItemStack saved = ItemStack.EMPTY;

		if (module.hasTag() && module.getTag().contains(SAVED_BLOCK_KEY))
			saved = ItemStack.of(module.getTag().getCompound(SAVED_BLOCK_KEY));

		moduleInventory.set(0, saved);
	}

	public void save() {
		ItemStack stack = moduleInventory.get(0);

		if (stack.isEmpty()) {
			if (module.hasTag())
				module.getTag().remove(SAVED_BLOCK_KEY);
		}
		else
			module.getOrCreateTag().put(SAVED_BLOCK_KEY, stack.save(new CompoundTag()));
	}

	@Override
	public ItemStack removeItem(int index, int size) {
		ItemStack stack = getItem(index);

		if (!stack.isEmpty()) {
			if (stack.getCount() > size)
				stack = stack.split(size);
			else
				setItem(index, ItemStack.EMPTY);

			setChanged();
		}

		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack stack = getItem(index);

		setItem(index, ItemStack.EMPTY);
		setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		moduleInventory.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > getMaxStackSize())
			stack.setCount(getMaxStackSize());

		setChanged();
	}

	@Override
	public void setChanged() {
		for (int i = 0; i < getContainerSize(); i++) {
			if (!getItem(i).isEmpty() && getItem(i).getCount() == 0)
				moduleInventory.set(i, ItemStack.EMPTY);
		}

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
			save();
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return true;
	}

	@Override
	public void clearContent() {}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : moduleInventory) {
			if (!stack.isEmpty())
				return false;
		}

		return true;
	}

	public void setMenu(DisguiseModuleMenu menu) {
		this.menu = menu;
	}

	public ItemStack getModule() {
		return module;
	}
}
