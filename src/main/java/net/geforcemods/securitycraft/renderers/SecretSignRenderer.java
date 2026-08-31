package net.geforcemods.securitycraft.renderers;

import net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the sign normally, but blanks a side's text in the render state before it is drawn whenever the viewing
 * player is not allowed to read that side. Vanilla's {@code AbstractSignRenderer} keeps its text-drawing hook
 * private in this version, so the swap happens on the extracted {@link SignRenderState} instead of on the block
 * entity, which also avoids the edit-side effects {@code SignBlockEntity#setText} carries.
 */
public class SecretSignRenderer extends SignRenderer {
	public SecretSignRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void extractRenderState(SignBlockEntity be, SignRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

		if (be instanceof SecretSignBlockEntity sign) {
			LocalPlayer player = Minecraft.getInstance().player;

			if (!sign.isPlayerAllowedToSeeText(player, true))
				state.frontText = new SignText();

			if (!sign.isPlayerAllowedToSeeText(player, false))
				state.backText = new SignText();
		}
	}
}
