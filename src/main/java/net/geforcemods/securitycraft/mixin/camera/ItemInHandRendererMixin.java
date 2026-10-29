package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.items.TaserItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer.HandRenderSelection;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow
	public abstract void renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int seed);

	@Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
	private void securitycraft$suppressHandsOnCamera(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo ci) {
		if (SecurityCraftClient.isPlayerMountedOnCamera())
			ci.cancel();
	}

	@Inject(method = "evaluateWhichHandsToRender", at = @At("HEAD"), cancellable = true)
	private static void securitycraft$onlyRenderMainHandWhenHoldingTaser(LocalPlayer player, CallbackInfoReturnable<HandRenderSelection> cir) {
		if (player.getMainHandItem().getItem() instanceof TaserItem)
			cir.setReturnValue(HandRenderSelection.RENDER_MAIN_HAND_ONLY);
		else if (player.getOffhandItem().getItem() instanceof TaserItem)
			cir.setReturnValue(HandRenderSelection.RENDER_OFF_HAND_ONLY);
	}

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void securitycraft$renderTaserInFirstPerson(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
		if (!player.isScoping() && stack.getItem() instanceof TaserItem && swingProgress < 0.001F) {
			boolean isMainHand = hand == InteractionHand.MAIN_HAND;
			HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
			boolean isRightHand = arm == HumanoidArm.RIGHT;

			poseStack.pushPose();
			poseStack.translate(0.02F, -0.4F, -0.5F);
			renderItem(player, stack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, buffer, combinedLight);
			poseStack.popPose();
			ci.cancel();
		}
	}
}
