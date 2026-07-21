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
	public final boolean isLvl1;
	private final SimpleContainer itemInventory = new SimpleContainer(2);
	private final Slot inputSlot;
	private final Slot resultSlot;

	public BlockReinforcerMenu(int windowId, Inventory inventory) {
		super(SCContent.BLOCK_REINFORCER_MENU, windowId);

		ItemStack selected = inventory.getSelectedItem();

		tool = selected.getItem() instanceof BlockReinforcerItem ? selected : inventory.getItem(Inventory.SLOT_OFFHAND);
		isLvl1 = tool.is(SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL1);

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

		Block reinforced = SCContent.reinforcedCounterpart(block);
		Block vanilla = SCContent.vanillaCounterpart(block);
		boolean toolReinforces = tool.getItem() instanceof BlockReinforcerItem item && item.isReinforcing();

		if (toolReinforces) {
			// A reinforcer: reinforce a vanilla block, or (non-Lvl1) unreinforce a reinforced block. Independent of the mode toggle.
			if (reinforced != null)
				return reinforced;

			if (!isLvl1 && vanilla != null)
				return vanilla;

			return null;
		}
		else
			// A remover: only unreinforce.
			return vanilla;
	}

	public boolean isReinforcing() {
		return tool.getItem() instanceof BlockReinforcerItem item && item.isReinforcing(tool);
	}

	public boolean canToggleMode() {
		return tool.getItem() instanceof BlockReinforcerItem item && item.canToggleMode(tool);
	}

	public ItemStack getResult() {
		return resultSlot.getItem();
	}

	@Override
	public boolean stillValid(Player player) {
		return !net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, tool.getItem()).isEmpty();
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

			if (stack.getCount() == copy.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, copy.copyWithCount(copy.getCount() - stack.getCount()));
		}

		return copy;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
		return target != resultSlot && super.canTakeItemForPickAll(carried, target);
	}

	@Override
	public void clicked(int slot, int dragType, net.minecraft.world.inventory.ClickType clickType, Player player) {
		if (!(slot >= 0 && getSlot(slot).getItem().getItem() instanceof BlockReinforcerItem))
			super.clicked(slot, dragType, clickType, player);
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

				resultSlot.set(new ItemStack(out, count));
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
			tool.hurtAndBreak(taken.getCount(), player, player.getMainHandItem() == tool ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);

			if (tool.isEmpty()) {
				if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
					serverPlayer.closeContainer();
			}
			else
				inputSlot.setChanged();
		}
	}
}
