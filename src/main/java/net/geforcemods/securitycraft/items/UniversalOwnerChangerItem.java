package net.geforcemods.securitycraft.items;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Transfers ownership of an owned block to whoever the tool is renamed to. Renaming it in an anvil sets the new
 * owner; an unnamed tool can still claim a block that has no owner yet, if the config allows it. 1:1 with the
 * upstream item, minus the briefcase and display-case branches, whose blocks this port does not have.
 */
public class UniversalOwnerChangerItem extends Item {
	public UniversalOwnerChangerItem(Item.Properties properties) {
		super(properties);
	}

	/**
	 * Fabric stand-in for Forge's {@code onItemUseFirst}, so the tool runs before the clicked block's own
	 * right-click handling. Only the server touches ownership; the client returns SUCCESS, which still sends the
	 * interaction packet — Fabric's client mixin only swallows it for CONSUME and FAIL.
	 */
	public static void registerUseCallback() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!(stack.getItem() instanceof UniversalOwnerChangerItem changer))
				return InteractionResult.PASS;

			if (level.isClientSide)
				return level.getBlockEntity(hitResult.getBlockPos()) instanceof IOwnable ? InteractionResult.SUCCESS : InteractionResult.PASS;

			return changer.changeOwner(stack, new UseOnContext(player, hand, hitResult));
		});
	}

	private InteractionResult changeOwner(ItemStack stack, UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Player player = ctx.getPlayer();
		String newOwner = stack.getHoverName().getString();

		if (!(level.getBlockEntity(pos) instanceof IOwnable ownable)) {
			message(player, "messages.securitycraft:universalOwnerChanger.cantChange", ChatFormatting.RED);
			return InteractionResult.FAIL;
		}

		Owner owner = ownable.getOwner();
		boolean isDefault = owner.isDefaultOwner();

		if (!ownable.isOwnedBy(player) && !isDefault) {
			message(player, "messages.securitycraft:universalOwnerChanger.notOwned", ChatFormatting.RED);
			return InteractionResult.FAIL;
		}

		if (!stack.hasCustomHoverName() && !isDefault) {
			message(player, "messages.securitycraft:universalOwnerChanger.noName", ChatFormatting.RED);
			return InteractionResult.FAIL;
		}

		if (isDefault) {
			if (!ConfigHandler.allowBlockClaim) {
				message(player, "messages.securitycraft:universalOwnerChanger.noBlockClaiming", ChatFormatting.RED);
				return InteractionResult.FAIL;
			}

			if (!stack.hasCustomHoverName())
				newOwner = player.getName().getString();
		}

		ServerPlayer target = PlayerUtils.getPlayerFromName(newOwner);

		ownable.setOwner(newOwner, target != null ? target.getUUID().toString() : "ownerUUID");
		level.sendBlockUpdated(pos, state, state, 3);

		//the new owner should not inherit the previous one's modules, so they are handed back to the world
		if (level.getBlockEntity(pos) instanceof IModuleInventory inv) {
			for (ModuleType moduleType : inv.getInsertedModules()) {
				ItemStack moduleStack = inv.getModule(moduleType);

				inv.removeModule(moduleType, false);
				Block.popResource(level, pos, moduleStack);
			}
		}

		PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.changed", newOwner), ChatFormatting.GREEN);
		return InteractionResult.SUCCESS;
	}

	private void message(Player player, String key, ChatFormatting color) {
		PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.getDescriptionId()), Utils.localize(key), color);
	}
}
