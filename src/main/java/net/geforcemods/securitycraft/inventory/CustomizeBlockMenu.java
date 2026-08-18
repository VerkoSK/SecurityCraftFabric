package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The Universal Block Modifier's menu: one slot per module the block accepts, plus the player's inventory. */
public class CustomizeBlockMenu extends AbstractContainerMenu {
	public final BlockEntity be;
	private final ContainerLevelAccess containerLevelAccess;
	private final int moduleSlots;

	public CustomizeBlockMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		super(SCContent.CUSTOMIZE_BLOCK_MENU, windowId);
		containerLevelAccess = ContainerLevelAccess.create(level, pos);
		be = level.getBlockEntity(pos);

		Container container = be instanceof IModuleInventory inv ? new ModuleContainer(inv) : new SimpleContainer(0);

		moduleSlots = container.getContainerSize();

		for (int i = 0; i < moduleSlots; i++) {
			addSlot(new ModuleSlot(container, i, 8 + i * 18, 20));
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 105 + row * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlot(new Slot(inventory, slot, 8 + slot * 18, 163));
		}
	}

	/** How many of the leading slots are module slots, so the screen knows which ones to outline. */
	public int moduleSlotCount() {
		return moduleSlots;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (slot == null || !slot.hasItem())
			return ItemStack.EMPTY;

		ItemStack slotStack = slot.getItem();
		ItemStack copy = slotStack.copy();

		if (index < moduleSlots) {
			if (!moveItemStackTo(slotStack, moduleSlots, slots.size(), true))
				return ItemStack.EMPTY;
		}
		else if (!moveItemStackTo(slotStack, 0, moduleSlots, false))
			return ItemStack.EMPTY;

		if (slotStack.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();

		return copy;
	}

	@Override
	public boolean stillValid(Player player) {
		return containerLevelAccess.evaluate((level, pos) -> level.getBlockEntity(pos) == be && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
	}

	/** Bridges the block entity's module list to a Container, so vanilla slot handling can drive it. */
	private static class ModuleContainer extends SimpleContainer {
		private final IModuleInventory inv;

		ModuleContainer(IModuleInventory inv) {
			super(inv.getMaxNumberOfModules());
			this.inv = inv;

			for (int i = 0; i < inv.getMaxNumberOfModules(); i++) {
				setItem(i, inv.getInventory().get(i));
			}
		}

		@Override
		public void setChanged() {
			super.setChanged();

			for (int i = 0; i < getContainerSize(); i++) {
				ItemStack before = inv.getInventory().get(i);
				ItemStack now = getItem(i);

				if (!ItemStack.matches(before, now)) {
					if (!before.isEmpty() && before.getItem() instanceof ModuleItem module)
						inv.removeModule(module.getModuleType(), false);

					inv.getInventory().set(i, now);

					if (!now.isEmpty() && now.getItem() instanceof ModuleItem module)
						inv.insertModule(now, false);
				}
			}
		}
	}

	/** Only module items belong in these slots, and only the ones this block actually accepts. */
	private class ModuleSlot extends Slot {
		ModuleSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return be instanceof IModuleInventory inv && stack.getItem() instanceof ModuleItem module && inv.acceptsModule(module.getModuleType());
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}
