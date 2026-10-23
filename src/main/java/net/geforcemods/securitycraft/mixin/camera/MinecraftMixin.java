package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.InteractionHand;

@Mixin(value = Minecraft.class, priority = 1100)
public class MinecraftMixin {
	/**
	 * Disallows players from pressing F5 (by default) to change to third person while being mounted to a camera
	 */
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"))
	private void securitycraft$preventCameraSwitching(Options options, CameraType newType) {
		if (!SecurityCraftClient.isPlayerMountedOnCamera())
			options.setCameraType(newType);
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void securitycraft$onStartUseItem(CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera()) {
			Minecraft mc = (Minecraft) (Object) this;
			if (mc.player != null) {
				for (InteractionHand hand : InteractionHand.values()) {
					if (mc.player.getItemInHand(hand).is(SCContent.CAMERA_MONITOR))
						SCContent.CAMERA_MONITOR.use(mc.level, mc.player, hand);
				}
			}
			ci.cancel();
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void securitycraft$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (SecurityCraftClient.isPlayerMountedOnCamera()) {
			cir.setReturnValue(false);
		}
	}
}
