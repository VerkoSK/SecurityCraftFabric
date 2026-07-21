package net.geforcemods.securitycraft.api;

import net.minecraft.server.level.ServerLevel;

/** A block entity guarded by a salted passcode (keypad, key panel). Drives a redstone pulse when the correct code is entered. */
public interface PasscodeProtected {
	boolean hasPasscode();

	void setPasscode(String passcode);

	boolean checkPasscode(String attempt);

	Owner getOwner();

	void activate(ServerLevel level);
}
