package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The Universal Block Modifier's menu: one slot per module the block accepts, plus the player's inventory.
 * Slot positions and quick-move ranges are 1:1 with upstream's {@code CustomizeBlockMenu}.
 */
public class CustomizeBlockMenu extends AbstractContainerMenu {
	public final IModuleInventory moduleInv;
	private final ContainerLevelAccess worldPosCallable;
	private int maxSlots;

	public CustomizeBlockMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		super(SCContent.CUSTOMIZE_BLOCK_MENU, windowId);
		moduleInv = (IModuleInventory) level.getBlockEntity(pos);
		worldPosCallable = ContainerLevelAccess.create(level, pos);
		addSlots(inventory);
	}

	private void addSlots(Inventory inventory) {
		int slotId = moduleInv.enableHack() ? 100 : 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; ++j) {
				addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlot(new Slot(inventory, i, 8 + i * 18, 142));
		}

		int[] x;

		switch (moduleInv.getMaxNumberOfModules()) {
			case 1 -> x = new int[] { 80 };
			case 2 -> x = new int[] { 70, 88 };
			case 3 -> x = new int[] { 62, 80, 98 };
			case 4 -> x = new int[] { 52, 70, 88, 106 };
			case 5 -> x = new int[] { 34, 52, 70, 88, 106 };
			case 6 -> x = new int[] { 16, 34, 52, 70, 88, 106 };
			default -> x = new int[0];
		}

		for (int i = 0; i < x.length; i++) {
			addSlot(new ModuleSlot(slotId++, x[i], 20));
		}

		maxSlots = 36 + moduleInv.getMaxNumberOfModules();
	}

	public int getMaxSlots() {
		return maxSlots;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (slot == null || !slot.hasItem())
			return ItemStack.EMPTY;

		ItemStack slotStack = slot.getItem();
		boolean isModule = slotStack.getItem() instanceof ModuleItem;
		ItemStack copy = slotStack.copy();

		if (index >= 36 && index <= maxSlots) { //module slots
			if (!moveItemStackTo(slotStack, 0, 36, true)) //main inventory + hotbar
				return ItemStack.EMPTY;
		}
		else if (index >= 27 && index <= 35) { //hotbar
			if (isModule && !moveItemStackTo(slotStack, 36, maxSlots, false)) //module slots
				return ItemStack.EMPTY;
			else if (!isModule && !moveItemStackTo(slotStack, 0, 27, false)) //main inventory
				return ItemStack.EMPTY;
		}
		else if (index <= 26) { //main inventory
			if (isModule && !moveItemStackTo(slotStack, 36, maxSlots, false)) //module slots
				return ItemStack.EMPTY;
			else if (!isModule && !moveItemStackTo(slotStack, 27, 36, false)) //hotbar
				return ItemStack.EMPTY;
		}

		if (slotStack.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();

		return copy;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(worldPosCallable, player, ((BlockEntity) moduleInv).getBlockState().getBlock());
	}

	/**
	 * A slot backed directly by the block's {@link IModuleInventory}, since Fabric has no equivalent of NeoForge's
	 * {@code SlotItemHandler}. Validity, stack limit and change propagation are delegated to the interface's own
	 * default methods, matching upstream's {@code CustomSlotItemHandler}.
	 */
	private class ModuleSlot extends Slot {
		private final int moduleIndex;

		ModuleSlot(int index, int x, int y) {
			super(new SimpleContainer(0), index, x, y);
			moduleIndex = index;
		}

		@Override
		public ItemStack getItem() {
			return moduleInv.getModuleInSlot(moduleIndex);
		}

		@Override
		public boolean hasItem() {
			return !getItem().isEmpty();
		}

		@Override
		public void set(ItemStack stack) {
			moduleInv.setStackInSlot(moduleIndex, stack);
			setChanged();
			broadcastChanges();
		}

		@Override
		public void setChanged() {
			if (moduleInv instanceof BlockEntity be)
				be.setChanged();
		}

		@Override
		public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
			if (!oldStack.isEmpty() && oldStack.getItem() instanceof ModuleItem module) {
				moduleInv.onModuleRemoved(oldStack, module.getModuleType(), false);
				broadcastChanges();
			}
		}

		@Override
		public ItemStack remove(int amount) {
			ItemStack stack = moduleInv.extractItem(moduleIndex, amount, false);

			if (!stack.isEmpty())
				broadcastChanges();

			return stack;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return moduleInv.isItemValid(moduleIndex, stack);
		}
	}
}
