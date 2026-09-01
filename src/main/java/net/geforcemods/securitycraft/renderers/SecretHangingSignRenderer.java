package net.geforcemods.securitycraft.renderers;

import net.geforcemods.securitycraft.blockentities.SecretHangingSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

/**
 * Ported from upstream's {@code SecretHangingSignRenderer}: the hanging secret sign only shows its text to players
 * allowed to read it. See {@link SecretSignRenderer} for why this blanks the render-state {@link SignText} rather
 * than hooking per-side text submission.
 */
public class SecretHangingSignRenderer extends HangingSignRenderer {
	public SecretHangingSignRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void extractRenderState(SignBlockEntity be, HangingSignRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

		if (be instanceof SecretHangingSignBlockEntity secretSign) {
			LocalPlayer player = Minecraft.getInstance().player;

			if (!secretSign.isPlayerAllowedToSeeText(player, true))
				state.frontText = new SignText();

			if (!secretSign.isPlayerAllowedToSeeText(player, false))
				state.backText = new SignText();
		}
	}
}
