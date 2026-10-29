package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(Shulker.class)
public class ShulkerMixin {
	@Redirect(method = "teleportSomewhere", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
	private boolean securitycraft$onShulkerTeleport(Level level, Entity entity, AABB aabb) {
		if (!level.noCollision(entity, aabb))
			return false;

		return !RiftStabilizerBlockEntity.onEntityTeleport(entity, entity.position(), aabb.getCenter(), TeleportationType.SHULKER);
	}
}
