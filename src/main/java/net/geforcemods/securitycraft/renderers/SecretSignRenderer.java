package net.geforcemods.securitycraft.renderers;

import net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

/**
 * Ported from upstream's {@code SecretSignRenderer}: the standing secret sign only shows its text to players
 * allowed to read it. Upstream/NeoForge suppress a single side's text through a {@code submitSignText} hook that
 * Fabric's sign renderer has no equivalent of, so this blanks the disallowed side's {@link SignText} in the
 * render state instead, which the vanilla renderer then draws as an empty sign.
 */
public class SecretSignRenderer extends StandingSignRenderer {
	public SecretSignRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void extractRenderState(SignBlockEntity be, StandingSignRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

		if (be instanceof SecretSignBlockEntity secretSign) {
			LocalPlayer player = Minecraft.getInstance().player;

			if (!secretSign.isPlayerAllowedToSeeText(player, true))
				state.frontText = new SignText();

			if (!secretSign.isPlayerAllowedToSeeText(player, false))
				state.backText = new SignText();
		}
	}
}
