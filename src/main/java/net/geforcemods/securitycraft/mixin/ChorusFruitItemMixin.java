package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(ChorusFruitItem.class)
public class ChorusFruitItemMixin {
	@Unique
	private boolean securitycraft$teleportBlocked = false;

	@Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z"))
	private boolean securitycraft$onChorusFruitTeleport(LivingEntity entity, double targetX, double targetY, double targetZ, boolean broadcastTeleport) {
		if (RiftStabilizerBlockEntity.onEntityTeleport(entity, entity.position(), new Vec3(targetX, targetY, targetZ), TeleportationType.CHORUS_FRUIT)) {
			securitycraft$teleportBlocked = true;
			return false;
		}

		return entity.randomTeleport(targetX, targetY, targetZ, broadcastTeleport);
	}

	@Inject(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z", shift = At.Shift.AFTER), cancellable = true)
	private void securitycraft$stopChorusFruitLoopIfBlocked(ItemStack stack, Level level, LivingEntity entityLiving, CallbackInfoReturnable<ItemStack> cir) {
		if (securitycraft$teleportBlocked) {
			securitycraft$teleportBlocked = false;
			cir.setReturnValue(stack);
		}
	}
}
