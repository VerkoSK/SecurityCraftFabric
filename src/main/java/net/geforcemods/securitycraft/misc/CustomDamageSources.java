package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/** Custom damage types. The type itself is defined by the datapack JSON in {@code data/securitycraft/damage_type/}. */
public class CustomDamageSources {
	public static final ResourceKey<DamageType> LASER = ResourceKey.create(Registries.DAMAGE_TYPE, SCContent.id("laser"));
	public static final ResourceKey<DamageType> INCORRECT_PASSCODE = ResourceKey.create(Registries.DAMAGE_TYPE, SCContent.id("incorrect_passcode"));

	private CustomDamageSources() {}

	public static DamageSource laser(RegistryAccess registryAccess) {
		return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(LASER));
	}

	public static DamageSource incorrectPasscode(RegistryAccess registryAccess) {
		return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(INCORRECT_PASSCODE));
	}
}
