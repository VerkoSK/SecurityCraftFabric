package net.geforcemods.securitycraft.items;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Opens the customize screen for the block it is used on, so its owner can fit modules and change its options. */
public class UniversalBlockModifierItem extends Item {
	public UniversalBlockModifierItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();

		if (!(level.getBlockEntity(pos) instanceof ICustomizable))
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		Player player = ctx.getPlayer();

		if (level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(player)) {
			PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_BLOCK_MODIFIER.getDescriptionId()), Utils.localize("messages.securitycraft:universalBlockModifier.notOwned"), ChatFormatting.RED);
			return InteractionResult.FAIL;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			Component title = level.getBlockState(pos).getBlock().getName();

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
