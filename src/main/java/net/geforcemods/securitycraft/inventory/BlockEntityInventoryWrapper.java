package net.geforcemods.securitycraft.inventory;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityInventoryWrapper<T extends BlockEntity & Predicate<ItemStack>> implements Container {
	private final T wrapped;
	private final AbstractContainerMenu menu;
	private ItemStack content = ItemStack.EMPTY;

	public BlockEntityInventoryWrapper(T wrapped, AbstractContainerMenu menu) {
		this.wrapped = wrapped;
		this.menu = menu;
	}

	@Override
	public void clearContent() {
		content = ItemStack.EMPTY;
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return content;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack toReturn = content.copy();

		if (content.getCount() >= count)
			toReturn.setCount(count);

		content.shrink(count);
		setChanged();
		return toReturn;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack temp = content;

		content = ItemStack.EMPTY;
		setChanged();
		return temp;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		if (canPlaceItem(index, stack)) {
			content = stack;
			setChanged();
		}
	}

	@Override
	public void setChanged() {
		wrapped.setChanged();
		menu.slotsChanged(this);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return wrapped.test(stack);
	}
}
