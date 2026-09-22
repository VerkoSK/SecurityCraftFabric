package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.KeycardHolderData;
import net.geforcemods.securitycraft.inventory.KeycardHolderMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Holds up to {@link #SLOTS} keycards; right-click to open its inventory. On 1.20.1 the contents live in the stack's
 * NBT; this port has no NBT tags, so they live in the {@link KeycardHolderData} data component instead.
 */
public class KeycardHolderItem extends Item {
	public static final int SLOTS = 6;

	public KeycardHolderItem(Properties properties) {
		super(properties);
	}

	public static NonNullList<ItemStack> getContents(ItemStack holder) {
		KeycardHolderData data = holder.getOrDefault(SCContent.KEYCARD_HOLDER_DATA, KeycardHolderData.sized(SLOTS));
		NonNullList<ItemStack> contents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

		for (int i = 0; i < SLOTS && i < data.cards().size(); i++)
			contents.set(i, data.cards().get(i));

		return contents;
	}

	public static void setContents(ItemStack holder, NonNullList<ItemStack> contents) {
		holder.set(SCContent.KEYCARD_HOLDER_DATA, new KeycardHolderData(java.util.List.copyOf(contents)));
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
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		openMenu(level, player, player.getItemInHand(hand));
		return InteractionResult.CONSUME;
	}

	/**
	 * Right-clicking a block (rather than air) never reaches {@link #use}: vanilla only calls it when the player's
	 * raycast hits nothing, so a block-only item that skips this override would silently never open its menu.
	 */
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		openMenu(ctx.getLevel(), ctx.getPlayer(), ctx.getItemInHand());
		return InteractionResult.CONSUME;
	}

	private void openMenu(Level level, Player player, ItemStack stack) {
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
	}
}
