package net.geforcemods.securitycraft.renderers;

import org.joml.Quaternionf;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.entity.camera.FrameFeedHandler;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SecurityCameraRenderer implements BlockEntityRenderer<SecurityCameraBlockEntity, SecurityCameraRenderState> {
	private static final Quaternionf POSITIVE_Y_180 = Axis.YP.rotationDegrees(180.0F);
	private static final Quaternionf POSITIVE_Y_90 = Axis.YP.rotationDegrees(90.0F);
	private static final Quaternionf NEGATIVE_Y_90 = Axis.YN.rotationDegrees(90.0F);
	private static final Quaternionf POSITIVE_X_180 = Axis.XP.rotationDegrees(180.0F);
	public static final Identifier TEXTURE = SCContent.id("textures/entity/security_camera/security_camera.png");
	private static final Identifier BEING_VIEWED_TEXTURE = SCContent.id("textures/entity/security_camera/security_camera_viewing.png");
	private final SecurityCameraModel model;

	public SecurityCameraRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new SecurityCameraModel(ctx.bakeLayer(SecurityCraftClient.SECURITY_CAMERA_LOCATION));
	}

	@Override
	public SecurityCameraRenderState createRenderState() {
		return new SecurityCameraRenderState();
	}

	@Override
	public void extractRenderState(SecurityCameraBlockEntity be, SecurityCameraRenderState state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
		if (FrameFeedHandler.amIBeingCaptured(be) || (PlayerUtils.isPlayerMountedOnCamera(Minecraft.getInstance().player) && Minecraft.getInstance().getCameraEntity().blockPosition().equals(be.getBlockPos())) || be.isDown()) {
			state.shouldSkip = true;
			return;
		}

		state.shouldSkip = false;
		state.isDisguised = be.isModuleEnabled(ModuleType.DISGUISE);
		if (!state.isDisguised) {
			BlockState blockState = be.getBlockState();
			if (blockState.getBlock() == SCContent.SECURITY_CAMERA)
				state.facing = blockState.getValue(SecurityCameraBlock.FACING);
			else
				state.facing = Direction.NORTH;

			state.cameraRotation = (float) Mth.lerp(partialTicks, be.getOriginalCameraRotation(), be.getCameraRotation());
			state.isShutDown = be.isShutDown();

			ItemStack lens = be.getLensContainer().getItem(0);
			if (lens.has(DataComponents.DYED_COLOR)) {
				state.hasColor = true;
				state.color = lens.get(DataComponents.DYED_COLOR).rgb();
			} else
				state.hasColor = false;

			state.texture = blockState.hasProperty(SecurityCameraBlock.BEING_VIEWED) && blockState.getValue(SecurityCameraBlock.BEING_VIEWED) ? BEING_VIEWED_TEXTURE : TEXTURE;
		}
	}

	@Override
	public void submit(SecurityCameraRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.shouldSkip || state.isDisguised)
			return;

		pose.pushPose();
		pose.translate(0.5D, 1.5D, 0.5D);

		Direction side = state.facing;
		if (side == Direction.NORTH)
			pose.mulPose(POSITIVE_Y_180);
		else if (side == Direction.EAST)
			pose.mulPose(POSITIVE_Y_90);
		else if (side == Direction.WEST)
			pose.mulPose(NEGATIVE_Y_90);

		pose.mulPose(POSITIVE_X_180);
		model.rotateCameraY(state.cameraRotation);

		if (state.isShutDown)
			model.rotateCameraX(0.9F);
		else
			model.rotateCameraX(SecurityCameraModel.DEFAULT_X_ROT);

		float r = 0.4392156862745098F, g = 1.0F, b = 1.0F;
		if (state.hasColor) {
			int color = state.color;
			r = ((color >> 0x10) & 0xFF) / 255.0F;
			g = ((color >> 0x8) & 0xFF) / 255.0F;
			b = (color & 0xFF) / 255.0F;
		} else
			model.cameraRotationPoint2.visible = false;

		int tint = ARGB.colorFromFloat(1.0F, r, g, b);
		collector.submitModelPart(model.attachment, pose, net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, false, false, -1, null, 0);
		collector.submitModelPart(model.stickTop, pose, net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, false, false, -1, null, 0);
		collector.submitModelPart(model.cameraRotationPoint1, pose, net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, false, false, -1, null, 0);
		if (state.hasColor) {
			collector.submitModelPart(model.cameraRotationPoint2, pose, net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, false, false, tint, null, 0);
		}
		pose.popPose();
	}
}
