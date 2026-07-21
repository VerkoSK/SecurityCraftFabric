package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class LensContainer extends SimpleContainer {
	private java.util.function.Consumer<net.minecraft.world.Container> changedListener;

	public LensContainer(int size) {
		super(size);
	}

	/**
	 * 26.x removed the vanilla {@code Container} listener mechanism ({@code addListener}/{@code containerChanged}),
	 * so the laser block wires up its lens-change callback here instead. Fired on normal changes ({@link #setChanged()});
	 * {@link #setItemExclusively(int, ItemStack)} deliberately bypasses it to avoid ping-ponging between paired lasers.
	 */
	public void setChangedListener(java.util.function.Consumer<net.minecraft.world.Container> listener) {
		changedListener = listener;
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (changedListener != null)
			changedListener.accept(this);
	}

	@Override
	public boolean canAddItem(ItemStack stack) {
		return stack.is(SCContent.LENS) && stack.has(DataComponents.DYED_COLOR);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return canAddItem(stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public void setItemExclusively(int index, ItemStack stack) {
		items.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > getMaxStackSize())
			stack.setCount(getMaxStackSize());
	}
}
