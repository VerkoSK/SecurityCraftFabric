package net.geforcemods.securitycraft.renderers;

import org.joml.Vector3f;

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
			new ItemTransform(new Vector3f(-90, 180, 0), new Vector3f(4.0F / 16.0F, 0, 5.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(-90, 180, 0), new Vector3f(-4.0F / 16.0F, 0, 5.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(9.75F / 16.0F, 8.5F / 16.0F, 4.0F / 16.0F), new Vector3f(0.68F, 0.68F, 0.68F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(0, 8.0F / 16.0F, 3.0F / 16.0F), new Vector3f(0.68F, 0.68F, 0.68F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(-2.0F / 16.0F, 7.0F / 16.0F, -6.25F / 16.0F), new Vector3f(0.25F, 0.25F, 0.25F)),
			new ItemTransform(new Vector3f(0, 225, 180), new Vector3f(1.5F / 16.0F, 8.0F / 16.0F, 0), new Vector3f(1.0F, 1.0F, 1.0F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(-4.0F / 16.0F, 6.0F / 16.0F, 2.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(-8.0F / 16.0F, 8.0F / 16.0F, 0.5F / 16.0F), new Vector3f(1.0F, 1.0F, 1.0F)));

	private SecurityCameraItemModel() {}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) {
		renderState.appendModelIdentityElement(this);
		ItemStackRenderState.LayerRenderState layer = renderState.newLayer();

		layer.setupSpecialModel(SecurityCameraSpecialModelRenderer.INSTANCE, null);
		layer.setTransform(TRANSFORMS.getTransform(displayContext));
	}
}
