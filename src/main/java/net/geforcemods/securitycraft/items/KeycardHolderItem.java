package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.inventory.KeycardHolderMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Holds up to {@link #SLOTS} keycards; right-click to open its inventory. Contents live in the stack's NBT. */
public class KeycardHolderItem extends Item {
	public static final int SLOTS = 6;
	private static final String TAG = "securitycraft:keycards";

	public KeycardHolderItem(Properties properties) {
		super(properties);
	}

	public static NonNullList<ItemStack> getContents(ItemStack holder) {
		NonNullList<ItemStack> contents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		CompoundTag tag = holder.getTagElement(TAG);

		if (tag != null && tag.contains("items")) {
			ListTag list = tag.getList("items", 10);

			for (int i = 0; i < list.size() && i < SLOTS; i++)
				contents.set(i, ItemStack.of(list.getCompound(i)));
		}

		return contents;
	}

	public static void setContents(ItemStack holder, NonNullList<ItemStack> contents) {
		CompoundTag tag = holder.getOrCreateTagElement(TAG);
		ListTag list = new ListTag();

		for (ItemStack stack : contents) {
			if (!stack.isEmpty())
				list.add(stack.save(new CompoundTag()));
		}

		tag.put("items", list);
	}

	public static int getCardCount(ItemStack holder) {
		int count = 0;

		for (ItemStack stack : getContents(holder)) {
			if (!stack.isEmpty())
				count++;
		}

		return count;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			player.openMenu(new MenuProvider() {
				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
					return new KeycardHolderMenu(id, inv, stack);
				}

				@Override
				public Component getDisplayName() {
					return stack.getHoverName();
				}
			});
		}

		return InteractionResultHolder.consume(stack);
	}
}
