package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ReinforcedPistonMovingBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.phys.Vec3;

public class ReinforcedPistonHeadRenderer implements BlockEntityRenderer<ReinforcedPistonMovingBlockEntity, PistonHeadRenderState> {
	public ReinforcedPistonHeadRenderer() {}

	public ReinforcedPistonHeadRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public PistonHeadRenderState createRenderState() {
		return new PistonHeadRenderState();
	}

	@Override
	public void extractRenderState(ReinforcedPistonMovingBlockEntity be, PistonHeadRenderState renderState, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		renderState.xOffset = be.getOffsetX(partialTicks);
		renderState.yOffset = be.getOffsetY(partialTicks);
		renderState.zOffset = be.getOffsetZ(partialTicks);
		renderState.block = null;
		renderState.base = null;
		BlockState state = be.getMovedState();
		Level level = be.getLevel();

		if (level != null && state != null && !state.isAir()) {
			BlockPos oppositePos = be.getBlockPos().relative(be.getMovementDirection().getOpposite());
			Holder<Biome> biome = level.getBiome(oppositePos);

			if (state.is(SCContent.REINFORCED_PISTON_HEAD) && be.getProgress(partialTicks) <= 4.0F) {
				state = state.setValue(PistonHeadBlock.SHORT, be.getProgress(partialTicks) <= 0.5F);
				renderState.block = createMovingBlock(oppositePos, state, biome, level);
			}
			else if (be.isSourcePiston() && !be.isExtending()) {
				PistonType pistonType = state.is(SCContent.REINFORCED_STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState headState = SCContent.REINFORCED_PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistonType).setValue(DirectionalBlock.FACING, state.getValue(DirectionalBlock.FACING));

				headState = headState.setValue(PistonHeadBlock.SHORT, be.getProgress(partialTicks) >= 0.5F);
				renderState.block = createMovingBlock(oppositePos, headState, biome, level);
				BlockPos renderPos = oppositePos.relative(be.getMovementDirection());
				state = state.setValue(PistonBaseBlock.EXTENDED, true);
				renderState.base = createMovingBlock(renderPos, state, biome, level);
			}
			else
				renderState.block = createMovingBlock(oppositePos, state, biome, level);
		}
	}

	@Override
	public void submit(PistonHeadRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		if (renderState.block == null)
			return;

		poseStack.pushPose();
		poseStack.translate(renderState.xOffset, renderState.yOffset, renderState.zOffset);
		collector.submitMovingBlock(poseStack, renderState.block);
		poseStack.popPose();

		if (renderState.base != null)
			collector.submitMovingBlock(poseStack, renderState.base);
	}

	private static MovingBlockRenderState createMovingBlock(BlockPos pos, BlockState state, Holder<Biome> biome, Level level) {
		MovingBlockRenderState movingBlock = new MovingBlockRenderState();
		movingBlock.randomSeedPos = pos;
		movingBlock.blockPos = pos;
		movingBlock.blockState = state;
		movingBlock.biome = biome;
		movingBlock.level = level;
		return movingBlock;
	}

	@Override
	public int getViewDistance() {
		return 68;
	}
}
