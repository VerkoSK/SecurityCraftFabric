package net.geforcemods.securitycraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class FullbrightBlockAndTintGetter implements BlockAndTintGetter {
	private final BlockAndTintGetter wrapped;

	public FullbrightBlockAndTintGetter(BlockAndTintGetter wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return wrapped.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return wrapped.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return wrapped.getFluidState(pos);
	}

	@Override
	public int getHeight() {
		return wrapped.getHeight();
	}

	@Override
	public int getMinY() {
		return wrapped.getMinY();
	}

	@Override
	public net.minecraft.world.level.CardinalLighting cardinalLighting() {
		return wrapped.cardinalLighting();
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return wrapped.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return colorResolver.getColor(Minecraft.getInstance().level.getBiome(pos).value(), pos.getX(), pos.getZ());
	}

	@Override
	public int getBrightness(LightLayer lightType, BlockPos pos) {
		return 15;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		return 15;
	}
}
