package net.geforcemods.securitycraft.misc;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.misc.ContainerLockData.LockedContainer;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * Registers the two global listeners that make a {@link ContainerLockData} lock actually stick: opening a locked
 * container goes through the same set/check passcode screens a real keypad chest uses (see
 * {@link net.geforcemods.securitycraft.network.NetworkHandler}), and only the owner can break it. Not part of
 * upstream SecurityCraft.
 */
public final class ContainerLockEnforcement {
	private ContainerLockEnforcement() {}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
				return InteractionResult.PASS;

			BlockPos pos = hitResult.getBlockPos();
			LockedContainer lock = ContainerLockData.get(serverLevel).get(pos);

			if (lock == null)
				return InteractionResult.PASS;

			if (!lock.hasPasscode()) {
				if (lock.getOwner().isOwner(player))
					NetworkHandler.openKeypadScreen(serverPlayer, pos, true, lock.getOwner().getName());
				else
					PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);
			}
			else {
				lock.setPendingOpener(player);
				NetworkHandler.openKeypadScreen(serverPlayer, pos, false, lock.getOwner().getName());
			}

			return InteractionResult.SUCCESS;
		});

		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide || player.isCreative() || player.isSpectator() || !(level instanceof ServerLevel serverLevel))
				return true;

			LockedContainer lock = ContainerLockData.get(serverLevel).get(pos);

			if (lock == null)
				return true;

			Owner owner = lock.getOwner();

			if (owner.isTreatedTheSameAs(new Owner(player)))
				return true;

			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:notOwned", owner.getName()), ChatFormatting.RED);
			return false;
		});

		//the lock was broken along with the block: forget it, instead of leaking a stale entry once another block replaces it
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!level.isClientSide && level instanceof ServerLevel serverLevel)
				ContainerLockData.get(serverLevel).unlock(pos);
		});
	}
}
