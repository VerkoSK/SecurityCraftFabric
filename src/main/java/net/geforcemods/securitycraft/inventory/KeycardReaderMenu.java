package net.geforcemods.securitycraft.inventory;

import java.util.Optional;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.util.TeamUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The keycard reader/lock's programming screen: one slot to hold the keycard being linked. */
public class KeycardReaderMenu extends AbstractContainerMenu {
	private final SimpleContainer itemInventory = new SimpleContainer(1);
	public final Slot keycardSlot;
	public final KeycardReaderBlockEntity be;
	private final ContainerLevelAccess worldPosCallable;

	/** Client-side constructor: reads the position the block entity's screen-opening data sent. */
	public KeycardReaderMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
		this(windowId, inventory, inventory.player.level(), buf.readBlockPos());
	}

	public KeycardReaderMenu(int windowId, Inventory inventory, Level level, BlockPos pos) {
		super(SCContent.KEYCARD_READER_MENU, windowId);

		be = (KeycardReaderBlockEntity) level.getBlockEntity(pos);
		worldPosCallable = ContainerLevelAccess.create(level, pos);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(inventory, 9 + j + i * 9, 8 + j * 18, 167 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlot(new Slot(inventory, i, 8 + i * 18, 225));
		}

		keycardSlot = addSlot(new Slot(itemInventory, 0, 35, 86) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				if (!(stack.getItem() instanceof KeycardItem) || stack.getItem() == SCContent.LIMITED_USE_KEYCARD)
					return false;

				if (!KeycardItem.isLinked(stack))
					return true;

				Owner keycardOwner = new Owner(KeycardItem.getOwnerName(stack), KeycardItem.getOwnerUUID(stack));

				return keycardOwner.getUUID().isEmpty() || TeamUtils.areOnSameTeam(be.getOwner(), keycardOwner) || keycardOwner.getUUID().equals(be.getOwner().getUUID());
			}
		});
	}

	public void link(Optional<String> usableBy) {
		ItemStack keycard = keycardSlot.getItem();

		if (!keycard.isEmpty()) {
			Owner owner = be.getOwner();

			KeycardItem.link(keycard, be.getSignature(), usableBy, owner.getName(), owner.getUUID());
		}
	}

	public void setKeycardUsesLeft(int usesLeft) {
		ItemStack keycard = keycardSlot.getItem();

		if (!keycard.isEmpty() && KeycardItem.isLimited(keycard))
			KeycardItem.setUsesLeft(keycard, usesLeft);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		clearContainer(player, itemInventory);
		be.setChanged();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int id) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = slots.get(id);

		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();

			slotStackCopy = slotStack.copy();

			if (id >= 36) {
				if (!moveItemStackTo(slotStack, 0, 36, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(slotStack, slotStackCopy);
			}
			else if (!moveItemStackTo(slotStack, 36, 37, false))
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
	public boolean stillValid(Player player) {
		return stillValid(worldPosCallable, player, be.getBlockState().getBlock());
	}
}
