package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MobMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void securitycraft$preventTargetingSentry(LivingEntity target, CallbackInfo ci) {
		if (target instanceof Sentry)
			ci.cancel();
	}
}
