package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** A block entity guarded by a salted passcode (keypad, key panel). Drives a redstone pulse when the correct code is entered. */
public interface PasscodeProtected {
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
	default void onIncorrectPasscodeEntered(Player player) {
		if (this instanceof IModuleInventory moduleInv) {
			if (moduleInv.isModuleEnabled(ModuleType.SMART))
				startCooldown();

			if (moduleInv.isModuleEnabled(ModuleType.HARMING) && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && serverPlayer.hurtServer(serverPlayer.serverLevel(), CustomDamageSources.incorrectPasscode(serverPlayer.level().registryAccess()), ConfigHandler.incorrectPasscodeDamage))
				serverPlayer.closeContainer();
		}
	}
}
