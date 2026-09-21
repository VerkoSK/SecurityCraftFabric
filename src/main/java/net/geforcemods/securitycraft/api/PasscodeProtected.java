package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** A block entity guarded by a salted passcode (keypad, key panel). Drives a redstone pulse when the correct code is entered. */
public interface PasscodeProtected extends Codebreakable {
	boolean hasPasscode();

	void setPasscode(String passcode);

	boolean checkPasscode(String attempt);

	Owner getOwner();

	void activate(ServerLevel level);

	/** Puts this block on cooldown (Smart module), during which no new code can be entered. */
	void startCooldown();

	/** The UNIX timestamp at which the cooldown ends. */
	long getCooldownEnd();

	/** Whether this block is currently on cooldown. */
	boolean isOnCooldown();

	/**
	 * Called when a player enters an incorrect passcode. With the Smart module installed, starts the cooldown;
	 * with the Harming module installed, damages the player by {@link ConfigHandler#incorrectPasscodeDamage}.
	 */
	/** The Codebreaker only works once a passcode has actually been set. */
	@Override
	default boolean shouldAttemptCodebreak(Player player) {
		if (!hasPasscode()) {
			net.geforcemods.securitycraft.util.PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);
			return false;
		}

		return !isOnCooldown();
	}

	/** A broken code activates the block just as if the correct code had been entered. */
	@Override
	default void useCodebreaker(Player player) {
		if (player.level() instanceof ServerLevel serverLevel)
			activate(serverLevel);
	}

	default void onIncorrectPasscodeEntered(Player player) {
		if (this instanceof IModuleInventory moduleInv) {
			if (moduleInv.isModuleEnabled(ModuleType.SMART))
				startCooldown();

			if (moduleInv.isModuleEnabled(ModuleType.HARMING) && player.hurt(CustomDamageSources.incorrectPasscode(player.level().registryAccess()), ConfigHandler.incorrectPasscodeDamage) && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
				serverPlayer.closeContainer();
		}
	}
}
