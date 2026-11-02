package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.geforcemods.securitycraft.entity.camera.FrameFeedHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(value = GameRenderer.class, priority = 1100)
public class GameRendererMixin {
	@Shadow
	@Final
	Minecraft minecraft;

	/**
	 * Makes sure distortion effects are not rendered in camera feeds
	 */
	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 2))
	private float securitycraft$disableFeedDistortion(float delta, float start, float end, Operation<Float> original) {
		if (FrameFeedHandler.isCapturingCamera())
			return 0.0F;
		else
			return original.call(delta, start, end);
	}

	/**
	 * Sets the correct fog distance for rendering a frame feed
	 */
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;"), index = 1)
	private int securitycraft$modifyFogRenderDistance(int renderDistanceChunks) {
		if (FrameFeedHandler.isCapturingCamera())
			return FrameFeedHandler.getFrameFeedViewDistance(null);

		return renderDistanceChunks;
	}

	/**
	 * Provides a hook for capturing the necessary levels for frame feeds. This is done immediately after main level
	 * rendering, but before GUI rendering, to fix screen flickering with Iris.
	 */
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V"))
	private void securitycraft$afterLevelRendering(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		FrameFeedHandler.captureFrameFeeds(deltaTracker);
	}
}
