package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.blockentities.SecretHangingSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the sign normally, but swaps a side's stored text out for a blank {@link SignText} before delegating to
 * vanilla's own renderer whenever the viewing player is not allowed to read that side, then restores it afterward.
 * Vanilla's {@code SignRenderer} no longer exposes a text-drawing hook to override directly in this version, and
 * {@link SignBlockEntity#setText} carries edit-side effects (re-validating click commands, marking the block
 * entity dirty) that a per-frame render swap shouldn't trigger, so the {@code frontText}/{@code backText} fields
 * are swapped directly through reflection instead.
 */
public class SecretHangingSignRenderer extends HangingSignRenderer {
	public SecretHangingSignRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(SignBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
		if (!(be instanceof SecretHangingSignBlockEntity sign)) {
			super.render(be, partialTicks, pose, bufferSource, packedLight, packedOverlay, cameraPos);
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		SignText originalFront = be.getFrontText();
		SignText originalBack = be.getBackText();
		boolean seesFront = sign.isPlayerAllowedToSeeText(player, true);
		boolean seesBack = sign.isPlayerAllowedToSeeText(player, false);

		if (!seesFront)
			SecretSignTextAccess.setFrontText(be, new SignText());

		if (!seesBack)
			SecretSignTextAccess.setBackText(be, new SignText());

		super.render(be, partialTicks, pose, bufferSource, packedLight, packedOverlay, cameraPos);

		if (!seesFront)
			SecretSignTextAccess.setFrontText(be, originalFront);

		if (!seesBack)
			SecretSignTextAccess.setBackText(be, originalBack);
	}
}
