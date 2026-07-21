package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wraps the laser/keypad block's baked model. When the block entity supplies a disguise {@link BlockState}
 * as render data, this emits that block's quads instead — a Fabric (FRAPI) reimplementation of the upstream
 * NeoForge {@code DisguisableDynamicBakedModel}, updated for the 1.21.5 {@link BlockStateModel} API.
 */
public class DisguisableBakedModel implements BlockStateModel, FabricBlockStateModel {
	private final BlockStateModel base;

	public DisguisableBakedModel(BlockStateModel base) {
		this.base = base;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
		Object data = ((FabricBlockView) blockView).getBlockEntityRenderData(pos);

		if (data instanceof BlockState disguised) {
			BlockStateModel disguiseModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(disguised);

			((FabricBlockStateModel) disguiseModel).emitQuads(emitter, blockView, pos, disguised, random, cullTest);
		}
		else
			((FabricBlockStateModel) base).emitQuads(emitter, blockView, pos, state, random, cullTest);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> parts) {
		base.collectParts(random, parts);
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return base.particleIcon();
	}
}
