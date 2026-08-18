package net.geforcemods.securitycraft.items;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.inventory.CustomizeBlockMenu;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Opens the customize screen for the block it is used on, so its owner can fit modules and change its options. */
public class UniversalBlockModifierItem extends Item {
	public UniversalBlockModifierItem(Item.Properties properties) {
		super(properties);
	}

	/**
	 * Fabric stand-in for Forge's {@code IForgeItem#onItemUseFirst}: {@link UseBlockCallback} runs before the clicked
	 * block's {@code use}, which is what makes a plain right-click open this screen instead of the block's own GUI
	 * (e.g. a keypad's passcode screen) winning. Follows the same shape as {@code MineRemoteAccessToolItem} and
	 * {@code UniversalOwnerChangerItem}.
	 */
	public static void registerUseCallback() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (stack.getItem() instanceof UniversalBlockModifierItem modifier)
				return modifier.onItemUseFirst(new UseOnContext(player, hand, hitResult));

			return InteractionResult.PASS;
		});
	}

	private InteractionResult onItemUseFirst(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockEntity be = level.getBlockEntity(pos);
		Player player = ctx.getPlayer();

		if (!(be instanceof IModuleInventory))
			return InteractionResult.PASS;

		if (be instanceof IOwnable ownable && !ownable.isOwnedBy(player)) {
			if (!level.isClientSide)
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", ownable.getOwner().getName()), ChatFormatting.RED);

			return InteractionResult.FAIL;
		}

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			Component title = be instanceof Nameable nameable ? nameable.getDisplayName() : Component.translatable(be.getBlockState().getBlock().getDescriptionId());

			serverPlayer.openMenu(new ExtendedScreenHandlerFactory() {
				@Override
				public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player p) {
					return new CustomizeBlockMenu(windowId, level, pos, inventory);
				}

				@Override
				public Component getDisplayName() {
					return title;
				}

				@Override
				public void writeScreenOpeningData(ServerPlayer p, FriendlyByteBuf buf) {
					buf.writeBlockPos(pos);
				}
			});
		}

		return InteractionResult.SUCCESS;
	}
}
