package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasscodeConvertible;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.misc.ContainerLockData;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Key Panel item (id {@code keypad_item}): the block item for the Key Panel block, which also converts a
 * registered {@link IPasscodeConvertible} block (e.g. a Frame) into its passcode-protected form (a keypad) when
 * right-clicked on it, consuming the required amount of key panels. 1:1 with upstream KeyPanelItem.
 * <p>
 * Extended past upstream: if nothing recognises the clicked block as convertible but it still holds an inventory
 * (any modded chest/barrel/etc that doesn't extend vanilla's {@code ChestBlock}, so it can't become a keypad chest),
 * right-clicking locks that position instead via {@link ContainerLockData} - not a real SecurityCraft block, just an
 * ownership record enforced by {@code ContainerLockEnforcement}. Right-clicking your own lock again removes it.
 */
public class KeyPanelItem extends BlockItem {
	public KeyPanelItem(Item.Properties properties) {
		super(SCContent.KEY_PANEL, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();

		if (!(level.getBlockEntity(pos) instanceof IOwnable ownable) || ownable.isOwnedBy(player)) {
			for (IPasscodeConvertible pc : SecurityCraftAPI.getRegisteredPasscodeConvertibles()) {
				if (pc.isUnprotectedBlock(state)) {
					int requiredKeyPanels = pc.getRequiredKeyPanels(state);

					if (requiredKeyPanels > stack.getCount()) {
						PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Component.translatable("messages.securitycraft:notEnoughKeyPanels", requiredKeyPanels), ChatFormatting.RED);
						return InteractionResult.FAIL;
					}

					if (pc.protect(player, level, pos)) {
						if (!player.isCreative())
							stack.shrink(requiredKeyPanels);

						level.playSound(null, pos, SCSounds.LOCK.event, SoundSource.BLOCKS, 1.0F, 1.0F);
						return InteractionResult.SUCCESS;
					}

					return InteractionResult.FAIL;
				}
			}

			if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof Container && !(level.getBlockEntity(pos) instanceof IOwnable)) {
				ContainerLockData data = ContainerLockData.get(serverLevel);
				ContainerLockData.LockedContainer lock = data.get(pos);

				if (lock != null) {
					if (!lock.getOwner().isTreatedTheSameAs(new Owner(player)))
						return InteractionResult.PASS; //not yours to unlock; fall through so this doesn't consume a key panel either

					data.unlock(pos);
				}
				else {
					data.lock(pos, new Owner(player));

					if (!player.isCreative())
						stack.shrink(1);
				}

				level.playSound(null, pos, SCSounds.LOCK.event, SoundSource.BLOCKS, 1.0F, 1.0F);
				return InteractionResult.SUCCESS;
			}
		}

		return super.useOn(ctx); //allow key panel to be placed when it did not convert or lock anything
	}
}
