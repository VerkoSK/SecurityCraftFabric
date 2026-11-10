package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;

@Mixin(value = LevelExtractor.class, priority = 1100)
public class LevelExtractorMixin {
	/**
	 * When the player is mounted to a camera, ensures the player entity is rendered instead of being skipped.
	 */
	@Redirect(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;", ordinal = 3))
	private Entity securitycraft$renderPlayerWhenMountedOnCamera(Camera camera) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			return Minecraft.getInstance().player;

		return camera.entity();
	}
}
