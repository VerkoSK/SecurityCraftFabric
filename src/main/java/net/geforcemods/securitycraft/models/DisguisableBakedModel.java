package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<Direction> cullTest) {
		Object data = ((FabricBlockView) blockView).getBlockEntityRenderData(pos);

		if (data instanceof BlockState disguised) {
			BakedModel disguiseModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(disguised);

			((FabricBakedModel) disguiseModel).emitBlockQuads(emitter, blockView, disguised, pos, randomSupplier, cullTest);
		}
		else
			((FabricBakedModel) base).emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		((FabricBakedModel) base).emitItemQuads(emitter, randomSupplier);
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
	public TextureAtlasSprite getParticleIcon() {
		return base.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return base.getTransforms();
	}
}
