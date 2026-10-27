package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(Gui.class)
public class GuiCameraMixin {
	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void securitycraft$suppressEffects(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			ci.cancel();
	}
}
