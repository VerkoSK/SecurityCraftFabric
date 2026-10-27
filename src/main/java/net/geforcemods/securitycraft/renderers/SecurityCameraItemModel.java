package net.geforcemods.securitycraft.renderers;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SecurityCameraItemModel implements ItemModel {
	public static final SecurityCameraItemModel INSTANCE = new SecurityCameraItemModel();
	private static final ItemTransforms TRANSFORMS = new ItemTransforms(
			new ItemTransform(new Vector3f(-90, 180, 0), new Vector3f(-4.0F / 16.0F, 0, 5.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(0, 8.0F / 16.0F, 3.0F / 16.0F), new Vector3f(0.68F, 0.68F, 0.68F)),
			new ItemTransform(new Vector3f(-90, 180, 0), new Vector3f(4.0F / 16.0F, 0, 5.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(9.75F / 16.0F, 8.5F / 16.0F, 4.0F / 16.0F), new Vector3f(0.68F, 0.68F, 0.68F)),
			new ItemTransform(new Vector3f(0, 180, 0), new Vector3f(-2.0F / 16.0F, 7.0F / 16.0F, -6.25F / 16.0F), new Vector3f(0.25F, 0.25F, 0.25F)),
			new ItemTransform(new Vector3f(0, 225, 180), new Vector3f(1.5F / 16.0F, 8.0F / 16.0F, 0), new Vector3f(1.0F, 1.0F, 1.0F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(-4.0F / 16.0F, 6.0F / 16.0F, 2.0F / 16.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
			new ItemTransform(new Vector3f(0, 0, 180), new Vector3f(-8.0F / 16.0F, 8.0F / 16.0F, 0.5F / 16.0F), new Vector3f(1.0F, 1.0F, 1.0F)));
	private static final BakedModel TRANSFORMS_ONLY_MODEL = new BakedModel() {
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource random) {
			return List.of();
		}

		@Override
		public boolean useAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean usesBlockLight() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
		}

		@Override
		public ItemTransforms getTransforms() {
			return TRANSFORMS;
		}
	};

	private SecurityCameraItemModel() {}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) {
		renderState.newLayer().setupSpecialModel(SecurityCameraSpecialModelRenderer.INSTANCE, null, TRANSFORMS_ONLY_MODEL);
	}
}
