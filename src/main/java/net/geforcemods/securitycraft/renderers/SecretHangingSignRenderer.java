package net.geforcemods.securitycraft.renderers;

import net.geforcemods.securitycraft.blockentities.SecretHangingSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the hanging sign normally, but blanks a side's text in the render state before it is drawn whenever the
 * viewing player is not allowed to read that side. See {@link SecretSignRenderer} for why the swap happens on the
 * render state rather than the block entity.
 */
public class SecretHangingSignRenderer extends HangingSignRenderer {
	public SecretHangingSignRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void extractRenderState(SignBlockEntity be, SignRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

		if (be instanceof SecretHangingSignBlockEntity sign) {
			LocalPlayer player = Minecraft.getInstance().player;

			if (!sign.isPlayerAllowedToSeeText(player, true))
				state.frontText = new SignText();

			if (!sign.isPlayerAllowedToSeeText(player, false))
				state.backText = new SignText();
		}
	}
}
