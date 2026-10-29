package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.entity.sentry.Bullet;
import net.geforcemods.securitycraft.models.BulletModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BulletRenderer extends EntityRenderer<Bullet> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SecurityCraft.MODID, "textures/entity/bullet.png");
	private final BulletModel model;

	public BulletRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		model = new BulletModel(ctx.bakeLayer(SecurityCraftClient.BULLET_LOCATION));
	}

	@Override
	public void render(Bullet entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
		VertexConsumer builder = buffer.getBuffer(model.renderType(getTextureLocation(entity)));

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
		model.renderToBuffer(pose, builder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		pose.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(Bullet entity) {
		return TEXTURE;
	}
}
