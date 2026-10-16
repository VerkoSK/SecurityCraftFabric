package net.geforcemods.securitycraft.inventory;

import org.lwjgl.glfw.GLFW;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DisguiseModuleMenu extends StateSelectorAccessMenu {
	private ModuleItemContainer inventory;
	private BlockState savedState = Blocks.AIR.defaultBlockState();
	private StandingOrWallType standingOrWall = StandingOrWallType.NONE;

	public DisguiseModuleMenu(int windowId, Inventory playerInventory) {
		this(windowId, playerInventory, new ModuleItemContainer(net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(playerInventory.player, SCContent.DISGUISE_MODULE)));
	}

	public DisguiseModuleMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(windowId, playerInventory, new ModuleItemContainer(playerInventory.player.getItemInHand(buf.readEnum(net.minecraft.world.InteractionHand.class))));
	}

	public DisguiseModuleMenu(int windowId, Inventory playerInventory, ModuleItemContainer moduleInventory) {
		super(SCContent.DISGUISE_MODULE_MENU, windowId);
		inventory = moduleInventory;
		inventory.setMenu(this);
		addSlot(new AddonSlot(inventory, 0, 79, 20));

		if (inventory.getModule().hasTag()) {
			savedState = NbtUtils.readBlockState(playerInventory.player.level().holderLookup(Registries.BLOCK), inventory.getModule().getTag().getCompound("SavedState"));
			standingOrWall = StandingOrWallType.values()[inventory.getModule().getTag().getInt("StandingOrWall")];
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
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

		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();

			slotStackCopy = slotStack.copy();

			if (index < inventory.getContainerSize()) {
				if (!moveItemStackTo(slotStack, inventory.getContainerSize(), 37, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(slotStack, slotStackCopy);
			}
			else if (index >= inventory.getContainerSize() && !moveItemStackTo(slotStack, 0, inventory.getContainerSize(), false))
				return ItemStack.EMPTY;

			if (slotStack.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (slotStack.getCount() == slotStackCopy.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
		}

		return slotStackCopy;
	}

	@Override
	public void clicked(int slot, int dragType, ClickType clickType, Player player) {
		if (slot >= 0 && getSlot(slot).getItem() == inventory.getModule() || clickType == ClickType.SWAP && dragType == GLFW.GLFW_KEY_1 + player.getInventory().selected && inventory.getModule() == player.getMainHandItem())
			return;
		else
			super.clicked(slot, dragType, clickType, player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public ModuleItemContainer getInventory() {
		return inventory;
	}

	@Override
	public ItemStack getStateStack() {
		return slots.get(0).getItem();
	}

	@Override
	public BlockState getSavedState() {
		return savedState;
	}

	@Override
	public StandingOrWallType getStandingOrWallType() {
		return standingOrWall;
	}

	public static class AddonSlot extends Slot {
		public AddonSlot(ModuleItemContainer inventory, int index, int xPos, int yPos) {
			super(inventory, index, xPos, yPos);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return itemStack.getItem() instanceof BlockItem;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}
