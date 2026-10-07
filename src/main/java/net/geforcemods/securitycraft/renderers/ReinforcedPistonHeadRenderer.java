package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ReinforcedPistonMovingBlockEntity;
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
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

public class ReinforcedPistonHeadRenderer implements BlockEntityRenderer<ReinforcedPistonMovingBlockEntity> {
	private BlockRenderDispatcher blockRenderer;

	public ReinforcedPistonHeadRenderer(BlockEntityRendererProvider.Context ctx) {
		blockRenderer = ctx.getBlockRenderDispatcher();
	}

	@Override
	public void render(ReinforcedPistonMovingBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		Level level = be.getLevel();

		if (level != null) {
			BlockPos oppositePos = be.getBlockPos().relative(be.getMovementDirection().getOpposite());
			BlockState state = be.getMovedState();

			if (state != null && !state.isAir()) {
				ModelBlockRenderer.enableCaching();
				poseStack.pushPose();
				poseStack.translate(be.getOffsetX(partialTicks), be.getOffsetY(partialTicks), be.getOffsetZ(partialTicks));

				if (state.is(SCContent.REINFORCED_PISTON_HEAD) && be.getProgress(partialTicks) <= 4.0F) {
					state = state.setValue(PistonHeadBlock.SHORT, be.getProgress(partialTicks) <= 0.5F);
					renderBlock(oppositePos, state, poseStack, buffer, level, false, combinedOverlay);
				}
				else if (be.isSourcePiston() && !be.isExtending()) {
					PistonType pistonType = state.is(SCContent.REINFORCED_STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
					BlockState headState = SCContent.REINFORCED_PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistonType).setValue(DirectionalBlock.FACING, state.getValue(DirectionalBlock.FACING));
					BlockPos renderPos = oppositePos.relative(be.getMovementDirection());

					headState = headState.setValue(PistonHeadBlock.SHORT, be.getProgress(partialTicks) >= 0.5F);
					renderBlock(oppositePos, headState, poseStack, buffer, level, false, combinedOverlay);
					poseStack.popPose();
					poseStack.pushPose();
					state = state.setValue(PistonBaseBlock.EXTENDED, true);
					renderBlock(renderPos, state, poseStack, buffer, level, true, combinedOverlay);
				}
				else
					renderBlock(oppositePos, state, poseStack, buffer, level, false, combinedOverlay);

				poseStack.popPose();
				ModelBlockRenderer.clearCache();
			}
		}
	}

	private void renderBlock(BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource buffer, Level level, boolean checkSides, int combinedOverlay) {
		if (blockRenderer == null)
			blockRenderer = Minecraft.getInstance().getBlockRenderer();

		RenderType renderType = ItemBlockRenderTypes.getMovingBlockRenderType(state);
		VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
		blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(state), state, pos, poseStack, vertexConsumer, checkSides, RandomSource.create(), state.getSeed(pos), combinedOverlay);
	}

	@Override
	public int getViewDistance() {
		return 68;
	}
}
