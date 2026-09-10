package net.geforcemods.securitycraft.items;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Right-click a passcode-protected block you own to open its set-passcode screen and change the code. Ported from the
 * upstream item; the briefcase branch is dropped (this port has no briefcase).
 */
public class UniversalKeyChangerItem extends Item {
	public UniversalKeyChangerItem(Item.Properties properties) {
		super(properties);
	}

	/** Fabric replacement for Forge's {@code onItemUseFirst} - fires before the block's own use handler. */
	public static void registerUseCallback() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!(stack.getItem() instanceof UniversalKeyChangerItem))
				return InteractionResult.PASS;

			BlockPos pos = hitResult.getBlockPos();

			if (!(level.getBlockEntity(pos) instanceof PasscodeProtected passcode))
				return InteractionResult.PASS;

			if (level.isClientSide)
				return InteractionResult.SUCCESS;

			if (!(player instanceof ServerPlayer serverPlayer))
				return InteractionResult.SUCCESS;

			if (passcode.getOwner().isOwner(serverPlayer) || serverPlayer.isCreative()) {
				NetworkHandler.openKeypadScreen(serverPlayer, pos, true, passcode.getOwner().getName());
				return InteractionResult.SUCCESS;
			}

			PlayerUtils.sendMessageToPlayer(serverPlayer, Utils.localize(SCContent.UNIVERSAL_KEY_CHANGER.getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", passcode.getOwner().getName()), ChatFormatting.RED);
			return InteractionResult.FAIL;
		});
	}
}
