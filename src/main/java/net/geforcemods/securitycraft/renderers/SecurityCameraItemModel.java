package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SecurityCameraItemModel implements ItemModel {
	public static final SecurityCameraItemModel INSTANCE = new SecurityCameraItemModel();
	private static final ItemTransforms TRANSFORMS = new ItemTransforms(
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM,
			ItemTransform.NO_TRANSFORM);

	private SecurityCameraItemModel() {}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) {
		ItemStackRenderState.LayerRenderState layer = renderState.newLayer();

		layer.setupSpecialModel(SecurityCameraSpecialModelRenderer.INSTANCE, null);
		layer.setTransform(TRANSFORMS.getTransform(displayContext));
	}
}