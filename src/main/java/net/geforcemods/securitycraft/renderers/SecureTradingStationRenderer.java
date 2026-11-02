package net.geforcemods.securitycraft.renderers;

import org.joml.Quaternionf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.geforcemods.securitycraft.blockentities.SecureTradingStationBlockEntity;
import net.geforcemods.securitycraft.blocks.SecureTradingStationBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SecureTradingStationRenderer implements BlockEntityRenderer<SecureTradingStationBlockEntity, SecureTradingStationRenderState> {
	private static final Quaternionf[] ROTATIONS = {
			new Quaternionf().rotateY((float) Math.PI), //south
			new Quaternionf().rotateY((float) Math.PI / 2.0F), //west
			new Quaternionf(), //north
			new Quaternionf().rotateY((float) -Math.PI / 2.0F) //east
	};
	private final ItemModelResolver itemModelResolver;

	public SecureTradingStationRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public SecureTradingStationRenderState createRenderState() {
		return new SecureTradingStationRenderState();
	}

	@Override
	public void extractRenderState(SecureTradingStationBlockEntity be, SecureTradingStationRenderState state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
		state.disguised = be.isModuleEnabled(ModuleType.DISGUISE);
		if (!state.disguised) {
			state.facing = be.getBlockState().getValue(SecureTradingStationBlock.FACING);
			ItemStack payment = be.getPaymentDisplay();
			ItemStack reward = be.getRewardDisplay();

			state.hasPayment = !payment.isEmpty();
			if (state.hasPayment) {
				state.paymentDisplay.clear();
				itemModelResolver.updateForTopItem(state.paymentDisplay, payment, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
			}

			state.hasReward = !reward.isEmpty();
			if (state.hasReward) {
				state.rewardDisplay.clear();
				itemModelResolver.updateForTopItem(state.rewardDisplay, reward, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
			}
		}
	}

	@Override
	public void submit(SecureTradingStationRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.disguised && state.facing != null) {
			if (state.hasPayment)
				renderItem(pose, state.paymentDisplay, state.facing, 0.225F, collector, state.lightCoords);

			if (state.hasReward)
				renderItem(pose, state.rewardDisplay, state.facing, -0.225F, collector, state.lightCoords);
		}
	}

	private void renderItem(PoseStack pose, net.minecraft.client.renderer.item.ItemStackRenderState itemState, Direction facing, float sideOffset, SubmitNodeCollector collector, int packedLight) {
		pose.pushPose();
		pose.translate(0.5F, 0.5F, 0.5F);
		pose.rotate(ROTATIONS[facing.get2DDataValue()]);
		pose.translate(sideOffset, 0.0F, 0.0F);
		pose.scale(0.35F, 0.35F, 0.35F);
		itemState.submit(pose, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}
