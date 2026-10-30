package net.geforcemods.securitycraft.entity.sentry;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ISentryBulletContainer {
	default Projectile getProjectileFromItem(Sentry sentry, Level level, Vec3 pos, ItemStack stack) {
		return new Bullet(level, sentry);
	}
}
