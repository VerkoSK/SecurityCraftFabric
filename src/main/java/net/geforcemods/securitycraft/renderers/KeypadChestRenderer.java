package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Renders the keypad chest with its own "active"/"inactive" artwork. 26.x's chest pipeline uses extracted render
 * states and a fixed {@code ChestMaterialType} enum, so this reuses vanilla's {@link ChestRenderer} (its models,
 * animation and extraction) and only overrides {@link #submit} to point at a SecurityCraft chest sprite on the
 * chest atlas instead of the vanilla one. Upstream reaches the same result through a Forge access transformer.
 */
public class KeypadChestRenderer extends ChestRenderer<ChestBlockEntity> {
	private static final SpriteId INACTIVE = chestSprite("inactive");
	private static final SpriteId ACTIVE = chestSprite("active");
	private static final SpriteId LEFT_INACTIVE = chestSprite("left_inactive");
	private static final SpriteId LEFT_ACTIVE = chestSprite("left_active");
	private static final SpriteId RIGHT_INACTIVE = chestSprite("right_inactive");
	private static final SpriteId RIGHT_ACTIVE = chestSprite("right_active");

	public KeypadChestRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void submit(ChestRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		pose.pushPose();
		pose.mulPose(modelTransformation(state.facing));

		float openness = state.open;

		openness = 1.0F - openness;
		openness = 1.0F - openness * openness * openness;

		boolean active = state.open >= 0.9F;
		SpriteId sprite = switch (state.type) {
			case LEFT -> active ? LEFT_ACTIVE : LEFT_INACTIVE;
			case RIGHT -> active ? RIGHT_ACTIVE : RIGHT_INACTIVE;
			default -> active ? ACTIVE : INACTIVE;
		};
		ChestModel model = models.select(state.type);

		collector.submitModel(model, Float.valueOf(openness), pose, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, sprites, 0, state.breakProgress);
		pose.popPose();
	}

	private static SpriteId chestSprite(String name) {
		return new SpriteId(Sheets.CHEST_SHEET, SCContent.id("entity/chest/" + name));
	}
}
