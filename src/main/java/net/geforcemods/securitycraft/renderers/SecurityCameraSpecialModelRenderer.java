package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;

public class SecurityCameraSpecialModelRenderer implements NoDataSpecialModelRenderer {
	public static final SecurityCameraSpecialModelRenderer INSTANCE = new SecurityCameraSpecialModelRenderer();
	private SecurityCameraModel model;

	private SecurityCameraSpecialModelRenderer() {}

	@Override
	public void submit(PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int seed) {
		if (model == null) {
			model = new SecurityCameraModel(Minecraft.getInstance().getEntityModels().bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
			model.cameraRotationPoint2.visible = false;
			model.rotateCameraY(0.0F);
			model.rotateCameraX(SecurityCameraModel.DEFAULT_X_ROT);
		}

		collector.submitModelPart(model.root(), pose, RenderTypes.entitySolid(SecurityCameraRenderer.TEXTURE), light, overlay, null, false, hasFoil, -1, null, seed);
	}

	@Override
	public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> consumer) {
		if (model == null) {
			model = new SecurityCameraModel(Minecraft.getInstance().getEntityModels().bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
			model.cameraRotationPoint2.visible = false;
			model.rotateCameraY(0.0F);
			model.rotateCameraX(SecurityCameraModel.DEFAULT_X_ROT);
		}

		PoseStack pose = new PoseStack();
		model.root().getExtentsForGui(pose, consumer);
	}
}
