package net.geforcemods.securitycraft.renderers;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.geforcemods.securitycraft.blockentities.RetinalScannerBlockEntity;
import net.geforcemods.securitycraft.blocks.RetinalScannerBlock;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RetinalScannerRenderer implements BlockEntityRenderer<RetinalScannerBlockEntity, RetinalScannerRenderState> {
	private static final float CORRECT_FACTOR = 1 / 550F;
	private final PlayerSkinRenderCache playerSkinRenderCache;

	public RetinalScannerRenderer(BlockEntityRendererProvider.Context ctx) {
		this.playerSkinRenderCache = ctx.playerSkinRenderCache();
	}

	@Override
	public RetinalScannerRenderState createRenderState() {
		return new RetinalScannerRenderState();
	}

	@Override
	public void extractRenderState(RetinalScannerBlockEntity be, RetinalScannerRenderState state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(RetinalScannerBlock.FACING);
		Direction rotation = blockState.getValue(RetinalScannerBlock.ROTATION);

		if (facing != null && rotation != null) {
			if (be.isModuleEnabled(ModuleType.DISGUISE) && ModuleItem.getBlockAddon(be.getModule(ModuleType.DISGUISE)) != null) {
				state.facing = null;
				state.rotation = null;
				return;
			}

			state.facing = facing;
			state.rotation = rotation;
			state.normalX = facing.getStepX();
			state.normalY = facing.getStepY();
			state.normalZ = facing.getStepZ();

			if (be.getPlayerProfile() != null)
				state.renderType = playerSkinRenderCache.getOrDefault(be.getPlayerProfile()).renderType();
			else
				state.renderType = PlayerSkinRenderCache.DEFAULT_PLAYER_SKIN_RENDER_TYPE;

			if (be.getLevel() != null) {
				BlockPos offsetPos = be.getBlockPos().relative(facing);
				state.combinedLight = LightTexture.pack(be.getLevel().getBrightness(LightLayer.BLOCK, offsetPos), be.getLevel().getBrightness(LightLayer.SKY, offsetPos));
			}
			else {
				state.combinedLight = LightTexture.FULL_BRIGHT;
			}
		}
		else {
			state.facing = null;
			state.rotation = null;
		}
	}

	@Override
	public void submit(RetinalScannerRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		Direction facing = state.facing;
		Direction rotation = state.rotation;

		if (facing == null || rotation == null || state.renderType == null)
			return;

		pose.pushPose();

		if (facing.getAxis().isHorizontal()) {
			switch (facing) {
				case NORTH:
					pose.translate(0.25F, 1.0F / 16.0F, 0.0F);
					break;
				case SOUTH:
					pose.translate(0.75F, 1.0F / 16.0F, 1.0F);
					pose.mulPose(Axis.YP.rotationDegrees(180.0F));
					break;
				case WEST:
					pose.translate(0.0F, 1.0F / 16.0F, 0.75F);
					pose.mulPose(Axis.YP.rotationDegrees(90.0F));
					break;
				case EAST:
					pose.translate(1.0F, 1.0F / 16.0F, 0.25F);
					pose.mulPose(Axis.YP.rotationDegrees(270.0F));
					break;
				default:
					break;
			}
		}
		else {
			pose.translate(0.5D, 0.5D, 0.5D);

			if (facing == Direction.DOWN) {
				pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
				pose.mulPose(Axis.ZP.rotationDegrees(rotation.toYRot() + 180.0F));
			}
			else if (facing == Direction.UP) {
				pose.mulPose(Axis.XP.rotationDegrees(90.0F));
				pose.mulPose(Axis.ZP.rotationDegrees(180.0F - rotation.toYRot()));
			}

			pose.translate(-0.25D, -0.4375D, -0.501D);
		}

		pose.scale(-1.0F, -1.0F, 1.0F);

		collector.submitCustomGeometry(pose, state.renderType, (vertexPose, vertexBuilder) -> {
			Matrix4f positionMatrix = vertexPose.pose();
			int normalX = state.normalX;
			int normalY = state.normalY;
			int normalZ = state.normalZ;
			int light = state.combinedLight;

			// face
			vertexBuilder.addVertex(positionMatrix, CORRECT_FACTOR, CORRECT_FACTOR * 1.5F, -0.001F).setColor(255, 255, 255, 255).setUv(0.125F, 0.25F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, CORRECT_FACTOR, -0.5F - CORRECT_FACTOR / 2F, -0.001F).setColor(255, 255, 255, 255).setUv(0.125F, 0.125F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, -0.5F - CORRECT_FACTOR, -0.5F - CORRECT_FACTOR / 2, -0.001F).setColor(255, 255, 255, 255).setUv(0.25F, 0.125F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, -0.5F - CORRECT_FACTOR, CORRECT_FACTOR * 1.5F, -0.001F).setColor(255, 255, 255, 255).setUv(0.25F, 0.25F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);

			// helmet
			vertexBuilder.addVertex(positionMatrix, CORRECT_FACTOR, CORRECT_FACTOR * 1.5F, -0.002F).setColor(255, 255, 255, 255).setUv(0.625F, 0.25F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, CORRECT_FACTOR, -0.5F - CORRECT_FACTOR / 2F, -0.002F).setColor(255, 255, 255, 255).setUv(0.625F, 0.125F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, -0.5F - CORRECT_FACTOR, -0.5F - CORRECT_FACTOR / 2, -0.002F).setColor(255, 255, 255, 255).setUv(0.75F, 0.125F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
			vertexBuilder.addVertex(positionMatrix, -0.5F - CORRECT_FACTOR, CORRECT_FACTOR * 1.5F, -0.002F).setColor(255, 255, 255, 255).setUv(0.75F, 0.25F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(vertexPose, normalX, normalY, normalZ);
		});

		pose.popPose();
	}
}
