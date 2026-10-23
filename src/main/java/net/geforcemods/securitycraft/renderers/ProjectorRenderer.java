package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.geforcemods.securitycraft.blockentities.DisguisableBlockEntity;
import net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ProjectorRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {
	public ProjectorRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(ProjectorBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		DisguisableBlockEntity.DISGUISED_BLOCK_RENDER_DELEGATE.tryRenderDelegate(be, partialTicks, pose, buffer, combinedLight, combinedOverlay);

		if (be.isActive() && !be.isEmpty()) {
			Level level = be.getLevel();
			BlockPos projectorPos = be.getBlockPos();
			BlockState state = be.getProjectedState();
			AABB projectedBlocksArea = be.getProjectedBlocksArea().deflate(0.1D);
			BlockPos minPos = BlockPos.containing(projectedBlocksArea.minX, projectedBlocksArea.minY, projectedBlocksArea.minZ);
			BlockPos maxPos = BlockPos.containing(projectedBlocksArea.maxX, projectedBlocksArea.maxY, projectedBlocksArea.maxZ);
			BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

			ModelBlockRenderer.enableCaching();

			for (BlockPos relativePos : BlockPos.betweenClosed(minPos, maxPos)) {
				BlockPos projectionPos = projectorPos.offset(relativePos);

				pose.pushPose();
				pose.translate(relativePos.getX(), relativePos.getY(), relativePos.getZ());

				if (be.isOverridingBlocks() || level.isEmptyBlock(projectionPos)) {
					RenderType renderType = ItemBlockRenderTypes.getMovingBlockRenderType(state);
					VertexConsumer consumer = buffer.getBuffer(renderType);
					dispatcher.getModelRenderer().tesselateBlock(level, dispatcher.getBlockModel(state), state, projectionPos, pose, consumer, true, RandomSource.create(), state.getSeed(projectionPos), combinedOverlay);

					ProjectorBlockEntity.PROJECTOR_RENDER_DELEGATE.tryRenderDelegate(be, partialTicks, pose, buffer, combinedLight, combinedOverlay);
				}

				pose.popPose();
			}

			ModelBlockRenderer.clearCache();
		}
	}

	@Override
	public boolean shouldRenderOffScreen(ProjectorBlockEntity be) {
		return true;
	}

	@Override
	public boolean shouldRender(ProjectorBlockEntity be, Vec3 cameraPosition) {
		return true; //Projected blocks should always render, regardless of distance
	}
}
