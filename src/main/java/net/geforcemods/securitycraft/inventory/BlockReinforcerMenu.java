package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.BlockReinforcerItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Container for the Universal Block Reinforcer/Remover: put a block in the input slot, take the converted block from the result slot (damages the tool). */
public class BlockReinforcerMenu extends AbstractContainerMenu {
	private final ItemStack tool;
	private final boolean reinforcing;
	private final SimpleContainer itemInventory = new SimpleContainer(2);
	private final Slot inputSlot;
	private final Slot resultSlot;

	public BlockReinforcerMenu(int windowId, Inventory inventory) {
		super(SCContent.BLOCK_REINFORCER_MENU, windowId);

		ItemStack selected = inventory.getSelected();

		tool = selected.getItem() instanceof BlockReinforcerItem ? selected : inventory.offhand.get(0);
		reinforcing = tool.getItem() instanceof BlockReinforcerItem item && item.isReinforcing();

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new Slot(inventory, 9 + j + i * 9, 8 + j * 18, 104 + i * 18));

		for (int i = 0; i < 9; i++)
			addSlot(new Slot(inventory, i, 8 + i * 18, 162));

		addSlot(inputSlot = new InputSlot(itemInventory, 0, 26, 20));
		addSlot(resultSlot = new ResultSlot(itemInventory, 1, 134, 20));
	}

	private Block target(ItemStack stack) {
		Block block = Block.byItem(stack.getItem());

		if (block == Blocks.AIR)
			return null;

		return reinforcing ? SCContent.reinforcedCounterpart(block) : SCContent.vanillaCounterpart(block);
	}

	public boolean isReinforcing() {
		return reinforcing;
	}

	@Override
	public boolean stillValid(Player player) {
		return !tool.isEmpty() && (player.getMainHandItem() == tool || player.getOffhandItem() == tool);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		resultSlot.set(ItemStack.EMPTY);
		clearContainer(player, itemInventory);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();

			copy = stack.copy();

			if (index >= 36) {
				if (!moveItemStackTo(stack, 0, 36, true))
					return ItemStack.EMPTY;
			}
			else if (!moveItemStackTo(stack, 36, 37, false))
				return ItemStack.EMPTY;

			if (stack.isEmpty())
				slot.setByPlayer(ItemStack.EMPTY);
			else
				slot.setChanged();
		}

		return copy;
	}

	private class InputSlot extends Slot {
		InputSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return target(stack) != null;
		}

		@Override
		public void setChanged() {
			super.setChanged();

			ItemStack stack = getItem();

			if (stack.isEmpty()) {
				resultSlot.set(ItemStack.EMPTY);
				return;
			}

			Block out = target(stack);

			if (out == null)
				resultSlot.set(ItemStack.EMPTY);
			else {
				int count = tool.isDamageableItem() ? Math.min(stack.getCount(), tool.getMaxDamage() - tool.getDamageValue()) : stack.getCount();

				resultSlot.set(new ItemStack(out, Math.max(1, count)));
			}
		}
	}

	private class ResultSlot extends Slot {
		ResultSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player player, ItemStack taken) {
			super.onTake(player, taken);
			inputSlot.getItem().shrink(taken.getCount());

			if (tool.isDamageableItem()) {
				for (int i = 0; i < taken.getCount(); i++) {
					int dmg = tool.getDamageValue() + 1;

					if (dmg >= tool.getMaxDamage()) {
						tool.setCount(0);
						break;
					}

					tool.setDamageValue(dmg);
				}
			}

			if (tool.isEmpty()) {
				if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
					serverPlayer.closeContainer();
			}
			else
				inputSlot.setChanged();
		}
	}
}
