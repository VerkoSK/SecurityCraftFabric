package net.geforcemods.securitycraft.api;

import java.util.ArrayList;
import java.util.List;

/** Minimal public API surface: the registry of {@link IPasscodeConvertible}s used by the Key Panel item. */
public final class SecurityCraftAPI {
	private static final List<IPasscodeConvertible> PASSCODE_CONVERTIBLES = new ArrayList<>();

	private SecurityCraftAPI() {}

	/** Registers a block-conversion handler for the Key Panel item. */
	public static void registerPasscodeConvertible(IPasscodeConvertible convertible) {
		PASSCODE_CONVERTIBLES.add(convertible);
	}

	public static List<IPasscodeConvertible> getRegisteredPasscodeConvertibles() {
		return PASSCODE_CONVERTIBLES;
	}
}
