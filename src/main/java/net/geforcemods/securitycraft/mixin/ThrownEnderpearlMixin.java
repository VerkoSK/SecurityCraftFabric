package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
	@Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z", shift = At.Shift.AFTER), cancellable = true)
	private void securitycraft$onEnderPearlHit(HitResult result, CallbackInfo ci) {
		ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
		Entity owner = self.getOwner();

		if (owner instanceof ServerPlayer player && !player.isSleeping()) {
			if (RiftStabilizerBlockEntity.onEntityTeleport(player, player.position(), self.position(), TeleportationType.ENDER_PEARL)) {
				self.discard();
				ci.cancel();
			}
		}
	}
}
