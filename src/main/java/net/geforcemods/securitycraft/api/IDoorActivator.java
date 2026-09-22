package net.geforcemods.securitycraft.api;

/**
 * Marks a block as able to open a reinforced door/trapdoor placed next to it while it's in its active
 * ({@code BlockStateProperties.POWERED}) state, bypassing ordinary redstone the same way a Keypad does. Simplified
 * port of upstream's {@code IDoorActivator} registry: implemented directly by the block instead of registered
 * separately, since every candidate here already carries a {@code POWERED} property to check.
 */
public interface IDoorActivator {
}
