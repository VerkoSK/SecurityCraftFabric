package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SecurityCameraItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private SecurityCameraModel model;

	@Override
	public void render(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (model == null) {
			Minecraft mc = Minecraft.getInstance();
			BlockEntityRendererProvider.Context renderingContext = new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font);

			model = new SecurityCameraModel(renderingContext.bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
			model.cameraRotationPoint2.visible = false;
			model.rotateCameraY(0.0F);
		}

		model.renderToBuffer(pose, buffer.getBuffer(RenderType.entitySolid(SecurityCameraRenderer.TEXTURE)), packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(1.0F, 0.4392156862745098F, 1.0F, 1.0F));
	}
}
