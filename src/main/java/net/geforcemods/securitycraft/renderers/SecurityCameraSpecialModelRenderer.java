package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SecurityCameraSpecialModelRenderer implements SpecialModelRenderer<Void> {
	public static final SecurityCameraSpecialModelRenderer INSTANCE = new SecurityCameraSpecialModelRenderer();
	private SecurityCameraModel model;

	private SecurityCameraSpecialModelRenderer() {}

	@Override
	public void render(Void unused, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay, boolean hasFoil) {
		if (model == null) {
			Minecraft mc = Minecraft.getInstance();
			BlockEntityRendererProvider.Context renderingContext = new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getItemModelResolver(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font);

			model = new SecurityCameraModel(renderingContext.bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
			model.cameraRotationPoint2.visible = false;
			model.rotateCameraY(0.0F);
		}

		model.renderToBuffer(pose, buffer.getBuffer(RenderType.entitySolid(SecurityCameraRenderer.TEXTURE)), combinedLight, combinedOverlay, ARGB.colorFromFloat(1.0F, 0.4392156862745098F, 1.0F, 1.0F));
	}

	@Override
	public void getExtents(java.util.Set<org.joml.Vector3f> extents) {}

	@Override
	public Void extractArgument(ItemStack stack) {
		return null;
	}
}
