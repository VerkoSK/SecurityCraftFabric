package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;

public class SecurityCameraSpecialModelRenderer implements NoDataSpecialModelRenderer {
	public static final SecurityCameraSpecialModelRenderer INSTANCE = new SecurityCameraSpecialModelRenderer();
	private SecurityCameraModel model;

	private SecurityCameraSpecialModelRenderer() {}

	@Override
	public void submit(ItemDisplayContext displayContext, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int seed) {
		if (model == null) {
			model = new SecurityCameraModel(Minecraft.getInstance().getEntityModels().bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
			model.cameraRotationPoint2.visible = false;
			model.rotateCameraY(0.0F);
		}

		int tint = ARGB.colorFromFloat(1.0F, 0.4392156862745098F, 1.0F, 1.0F);
		collector.submitCustomGeometry(pose, RenderType.entitySolid(SecurityCameraRenderer.TEXTURE), (vertexPose, vertexBuilder) -> {
			model.renderToBuffer(pose, vertexBuilder, light, overlay, tint);
		});
	}

	@Override
	public void getExtents(java.util.Set<org.joml.Vector3f> extents) {}
}
