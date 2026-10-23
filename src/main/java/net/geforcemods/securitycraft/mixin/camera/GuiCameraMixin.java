package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.PlayerRideableJumping;

@Mixin(Gui.class)
public class GuiCameraMixin {
	@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
	private void securitycraft$suppressExperienceBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			ci.cancel();
	}

	@Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
	private void securitycraft$suppressJumpMeter(PlayerRideableJumping rideable, GuiGraphics guiGraphics, int x, CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			ci.cancel();
	}

	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void securitycraft$suppressEffects(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			ci.cancel();
	}
}
