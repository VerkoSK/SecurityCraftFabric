package net.geforcemods.securitycraft.renderers;

import org.joml.Vector3f;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The item model for the keypad chest: {@link KeypadChestSpecialModelRenderer} plus the display transforms this
 * port's old-format keypad_chest.json item model used to carry (translation values pre-divided by 16, matching how
 * the old format's deserializer scaled them). Wired in through
 * {@link net.geforcemods.securitycraft.mixin.ItemModelResolverMixin}.
 */
public class KeypadChestItemModel implements ItemModel {
	public static final KeypadChestItemModel INSTANCE = new KeypadChestItemModel();
	private static final ItemTransforms TRANSFORMS = new ItemTransforms(
			ItemTransform.NO_TRANSFORM,
			new ItemTransform(new Vector3f(75, 315, 0), new Vector3f(0, 2.5F / 16.0F, 0), new Vector3f(0.375F, 0.375F, 0.375F)),
			ItemTransform.NO_TRANSFORM,
			new ItemTransform(new Vector3f(0, 315, 0), new Vector3f(0, 0, 0), new Vector3f(0.4F, 0.4F, 0.4F)),
			new ItemTransform(new Vector3f(0, 180, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)),
			new ItemTransform(new Vector3f(30, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.625F, 0.625F, 0.625F)),
			new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 3.0F / 16.0F, 0), new Vector3f(0.25F, 0.25F, 0.25F)),
			new ItemTransform(new Vector3f(0, 180, 0), new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.5F, 0.5F)));

	private KeypadChestItemModel() {}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) {
		ItemStackRenderState.LayerRenderState layer = renderState.newLayer();

		layer.setupSpecialModel(KeypadChestSpecialModelRenderer.INSTANCE, null);
		layer.setTransform(TRANSFORMS.getTransform(displayContext));
	}
}
