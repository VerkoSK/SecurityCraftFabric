package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ProjectorRenderer implements BlockEntityRenderer<ProjectorBlockEntity, ProjectorRenderState> {
	public ProjectorRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public ProjectorRenderState createRenderState() {
		return new ProjectorRenderState();
	}

	@Override
	public void extractRenderState(ProjectorBlockEntity be, ProjectorRenderState state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
		state.relativePositions.clear();
		state.active = be.isActive() && !be.isEmpty();
		if (!state.active)
			return;

		state.projectedState = be.getProjectedState();
		Level level = be.getLevel();
		BlockPos projectorPos = be.getBlockPos();
		AABB projectedBlocksArea = be.getProjectedBlocksArea().deflate(0.1D);
		BlockPos minPos = BlockPos.containing(projectedBlocksArea.minX, projectedBlocksArea.minY, projectedBlocksArea.minZ);
		BlockPos maxPos = BlockPos.containing(projectedBlocksArea.maxX, projectedBlocksArea.maxY, projectedBlocksArea.maxZ);

		for (BlockPos relativePos : BlockPos.betweenClosed(minPos, maxPos)) {
			BlockPos projectionPos = projectorPos.offset(relativePos);
			if (be.isOverridingBlocks() || (level != null && level.isEmptyBlock(projectionPos))) {
				state.relativePositions.add(relativePos.immutable());
			}
		}

		if (level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel && state.projectedState != null) {
			net.minecraft.client.renderer.block.MovingBlockRenderState mb = new net.minecraft.client.renderer.block.MovingBlockRenderState();
			mb.randomSeedPos = projectorPos;
			mb.blockPos = projectorPos;
			mb.blockState = state.projectedState;
			mb.biome = clientLevel.getBiome(projectorPos);
			mb.cardinalLighting = clientLevel.cardinalLighting();
			mb.lightEngine = clientLevel.getLightEngine();
			state.movingBlock = mb;
		}
	}

	@Override
	public void submit(ProjectorRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.active || state.projectedState == null)
			return;

		if (state.movingBlock == null)
			return;

		for (BlockPos relativePos : state.relativePositions) {
			pose.pushPose();
			pose.translate(relativePos.getX(), relativePos.getY(), relativePos.getZ());
			collector.submitMovingBlock(pose, state.movingBlock);
			pose.popPose();
		}
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public boolean shouldRender(ProjectorBlockEntity be, Vec3 cameraPosition) {
		return true;
	}
}
