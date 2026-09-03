package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Renders the keypad chest with its own "active"/"inactive" artwork. This Minecraft version's chest pipeline uses
 * extracted render states and a fixed {@code ChestMaterialType} enum, so this reuses vanilla's {@link ChestRenderer}
 * (its models, animation and extraction) and only overrides {@link #submit} to pick a SecurityCraft chest material
 * off the chest atlas instead of the vanilla one. Upstream reaches the same result through a Forge access transformer.
 */
public class KeypadChestRenderer extends ChestRenderer<ChestBlockEntity> {
	private static final Material INACTIVE = chestMaterial("inactive");
	private static final Material ACTIVE = chestMaterial("active");
	private static final Material LEFT_INACTIVE = chestMaterial("left_inactive");
	private static final Material LEFT_ACTIVE = chestMaterial("left_active");
	private static final Material RIGHT_INACTIVE = chestMaterial("right_inactive");
	private static final Material RIGHT_ACTIVE = chestMaterial("right_active");

	public KeypadChestRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void submit(ChestRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		pose.pushPose();
		pose.translate(0.5F, 0.5F, 0.5F);
		pose.mulPose(Axis.YP.rotationDegrees(-state.angle));
		pose.translate(-0.5F, -0.5F, -0.5F);

		float openness = state.open;

		openness = 1.0F - openness;
		openness = 1.0F - openness * openness * openness;

		boolean active = state.open >= 0.9F;
		Material material = switch (state.type) {
			case LEFT -> active ? LEFT_ACTIVE : LEFT_INACTIVE;
			case RIGHT -> active ? RIGHT_ACTIVE : RIGHT_INACTIVE;
			default -> active ? ACTIVE : INACTIVE;
		};
		RenderType renderType = material.renderType(RenderType::entityCutout);
		TextureAtlasSprite sprite = materials.get(material);
		ChestModel model = switch (state.type) {
			case LEFT -> doubleLeftModel;
			case RIGHT -> doubleRightModel;
			default -> singleModel;
		};

		collector.submitModel(model, Float.valueOf(openness), pose, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, 0, state.breakProgress);
		pose.popPose();
	}

	private static Material chestMaterial(String name) {
		return new Material(Sheets.CHEST_SHEET, SCContent.id("entity/chest/" + name));
	}
}
