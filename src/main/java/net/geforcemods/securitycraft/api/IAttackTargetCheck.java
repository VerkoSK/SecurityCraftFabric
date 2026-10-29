package net.geforcemods.securitycraft.api;

import net.minecraft.world.entity.Entity;

/**
 * Defines a callback that checks whether a Sentry can attack the given entity.
 * Callbacks can be registered via {@link SecurityCraftAPI#registerSentryAttackTargetCheck}.
 */
@FunctionalInterface
public interface IAttackTargetCheck {
	/**
	 * Checks if a Sentry is allowed to attack the given entity. Returning false does not guarantee that the Sentry will not
	 * attack this entity!
	 *
	 * @param potentialTarget The entity that the Sentry wants to attack
	 * @return true if the Sentry is allowed to attack this entity, false otherwise
	 */
	public boolean canAttack(Entity potentialTarget);
}
