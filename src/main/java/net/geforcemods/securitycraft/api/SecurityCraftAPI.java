package net.geforcemods.securitycraft.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.geforcemods.securitycraft.misc.ConfigAttackTargetCheck;

/** Public API surface: registries for {@link IPasscodeConvertible}s and {@link IAttackTargetCheck}s. */
public final class SecurityCraftAPI {
	private static final List<IPasscodeConvertible> PASSCODE_CONVERTIBLES = new ArrayList<>();
	private static final List<IAttackTargetCheck> SENTRY_ATTACK_TARGET_CHECKS = new ArrayList<>(List.of(ConfigAttackTargetCheck.INSTANCE));

	private SecurityCraftAPI() {}

	/** Registers a block-conversion handler for the Key Panel item. */
	public static void registerPasscodeConvertible(IPasscodeConvertible convertible) {
		PASSCODE_CONVERTIBLES.add(convertible);
	}

	public static List<IPasscodeConvertible> getRegisteredPasscodeConvertibles() {
		return PASSCODE_CONVERTIBLES;
	}

	public static void registerSentryAttackTargetCheck(IAttackTargetCheck check) {
		if (!SENTRY_ATTACK_TARGET_CHECKS.contains(check))
			SENTRY_ATTACK_TARGET_CHECKS.add(check);
	}

	public static List<IAttackTargetCheck> getRegisteredSentryAttackTargetChecks() {
		return Collections.unmodifiableList(SENTRY_ATTACK_TARGET_CHECKS);
	}
}
