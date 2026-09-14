package net.geforcemods.securitycraft.misc;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;

/**
 * Registers the two global listeners that make a {@link ContainerLockData} lock actually stick: a non-owner can
 * neither interact with nor break a locked position, on any block from any mod. Not part of upstream SecurityCraft.
 */
public final class ContainerLockEnforcement {
	private ContainerLockEnforcement() {}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide || player.isCreative() || player.isSpectator() || !(level instanceof ServerLevel serverLevel))
				return InteractionResult.PASS;

			ContainerLockData data = ContainerLockData.get(serverLevel);
			var pos = hitResult.getBlockPos();

			if (!data.isLocked(pos))
				return InteractionResult.PASS;

			Owner owner = data.getOwner(pos);

			if (owner.isTreatedTheSameAs(new Owner(player)))
				return InteractionResult.PASS;

			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:notOwned", owner.getName()), ChatFormatting.RED);
			return InteractionResult.FAIL;
		});

		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide || player.isCreative() || player.isSpectator() || !(level instanceof ServerLevel serverLevel))
				return true;

			ContainerLockData data = ContainerLockData.get(serverLevel);

			if (!data.isLocked(pos))
				return true;

			Owner owner = data.getOwner(pos);

			if (owner.isTreatedTheSameAs(new Owner(player)))
				return true;

			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:notOwned", owner.getName()), ChatFormatting.RED);
			return false;
		});

		//the owner broke their own lock: forget it, instead of leaking a stale entry once another block replaces it
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!level.isClientSide && level instanceof ServerLevel serverLevel)
				ContainerLockData.get(serverLevel).unlock(pos);
		});
	}
}
