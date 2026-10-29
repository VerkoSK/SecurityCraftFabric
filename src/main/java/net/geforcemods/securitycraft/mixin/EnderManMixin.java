package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;

@Mixin(EnderMan.class)
public class EnderManMixin {
	@Inject(method = "teleport(DDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;randomTeleport(DDDZ)Z"), cancellable = true)
	private void securitycraft$onEnderManTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		EnderMan self = (EnderMan) (Object) this;

		if (RiftStabilizerBlockEntity.onEntityTeleport(self, self.position(), new Vec3(x, y, z), TeleportationType.ENDERMAN))
			cir.setReturnValue(false);
	}
}
