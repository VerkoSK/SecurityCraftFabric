package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.api.IAttackTargetCheck;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.world.entity.Entity;

public class ConfigAttackTargetCheck implements IAttackTargetCheck {
	public static final ConfigAttackTargetCheck INSTANCE = new ConfigAttackTargetCheck();

	private ConfigAttackTargetCheck() {}

	@Override
	public boolean canAttack(Entity potentialTarget) {
		return ConfigHandler.SERVER.sentryAttackableEntitiesAllowlist.get().contains(Utils.getRegistryName(potentialTarget.getType()).toString());
	}
}
