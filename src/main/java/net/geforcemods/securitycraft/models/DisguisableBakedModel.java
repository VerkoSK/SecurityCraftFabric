package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wraps the laser/keypad block's baked model. When the block entity supplies a disguise {@link BlockState}
 * as render data, this emits that block's quads instead — a Fabric (FRAPI) reimplementation of the upstream
 * NeoForge {@code DisguisableDynamicBakedModel}, updated for the 26.x {@link BlockStateModel} API
 * (block models are now looked up via the {@code ModelManager}'s {@code BlockStateModelSet}).
 */
public class DisguisableBakedModel implements BlockStateModel {
	private final BlockStateModel base;

	public DisguisableBakedModel(BlockStateModel base) {
		this.base = base;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
		Object data = ((FabricBlockGetter) blockView).getBlockEntityRenderData(pos);

		if (data instanceof BlockState disguised) {
			BlockStateModel disguiseModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(disguised);

			disguiseModel.emitQuads(emitter, blockView, pos, disguised, random, cullTest);
		}
		else
			base.emitQuads(emitter, blockView, pos, state, random, cullTest);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
		base.collectParts(random, parts);
	}

	@Override
	public Material.Baked particleMaterial() {
		return base.particleMaterial();
	}

	@Override
	public int materialFlags() {
		return base.materialFlags();
	}
}
