package net.geforcemods.securitycraft.renderers;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.geforcemods.securitycraft.blockentities.DisguisableBlockEntity;
import net.geforcemods.securitycraft.blockentities.TrophySystemBlockEntity;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class TrophySystemRenderer implements BlockEntityRenderer<TrophySystemBlockEntity> {
	public TrophySystemRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(TrophySystemBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		DisguisableBlockEntity.DISGUISED_BLOCK_RENDER_DELEGATE.tryRenderDelegate(be, partialTicks, pose, buffer, combinedLight, combinedOverlay);

		Projectile target = be.getTarget();

		if (target == null)
			return;

		Matrix4f positionMatrix = pose.last().pose();
		VertexConsumer builder = buffer.getBuffer(RenderType.lines());
		BlockPos pos = be.getBlockPos();
		ItemStack lens = be.getLensContainer().getItem(0);
		int r = 255, g = 0, b = 0;

		if (Utils.hasLensColor(lens)) {
			int color = Utils.getLensColor(lens);

			r = (color >> 0x10) & 0xFF;
			g = (color >> 0x8) & 0xFF;
			b = color & 0xFF;
		}

		//draws a line between the trophy system and the projectile that it's targeting
		builder.vertex(positionMatrix, 0.5F, 0.75F, 0.5F).color(r, g, b, 255).normal(1.0F, 1.0F, 1.0F).endVertex();
		builder.vertex(positionMatrix, (float) (target.getX() - pos.getX()), (float) (target.getY() - pos.getY()), (float) (target.getZ() - pos.getZ())).color(r, g, b, 255).normal(1.0F, 1.0F, 1.0F).endVertex();
	}

	@Override
	public boolean shouldRenderOffScreen(TrophySystemBlockEntity be) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return TrophySystemBlockEntity.RENDER_DISTANCE;
	}

	@Override
	public boolean shouldRender(TrophySystemBlockEntity be, Vec3 cameraPos) {
		return Vec3.atCenterOf(be.getBlockPos()).closerThan(cameraPos, getViewDistance());
	}
}
