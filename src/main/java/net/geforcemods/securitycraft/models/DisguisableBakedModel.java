package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wraps the laser block's baked model. When the laser's block entity supplies a disguise {@link BlockState}
 * as render data, this emits that block's quads instead — a Fabric (FRAPI) reimplementation of the upstream
 * NeoForge {@code DisguisableDynamicBakedModel}.
 */
public class DisguisableBakedModel implements BakedModel, FabricBakedModel {
	private final BakedModel base;

	public DisguisableBakedModel(BakedModel base) {
		this.base = base;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		Object data = ((FabricBlockView) blockView).getBlockEntityRenderData(pos);

		if (data instanceof BlockState disguised) {
			BakedModel disguiseModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(disguised);

			((FabricBakedModel) disguiseModel).emitBlockQuads(blockView, disguised, pos, randomSupplier, context);
		}
		else
			((FabricBakedModel) base).emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		((FabricBakedModel) base).emitItemQuads(stack, randomSupplier, context);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
		return base.getQuads(state, side, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return base.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return base.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return base.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return base.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return base.getParticleIcon();
	}

	@Override
	public ItemOverrides getOverrides() {
		return base.getOverrides();
	}

	@Override
	public ItemTransforms getTransforms() {
		return base.getTransforms();
	}
}
