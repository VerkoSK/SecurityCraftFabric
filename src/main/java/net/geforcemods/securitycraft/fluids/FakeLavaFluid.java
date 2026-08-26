package net.geforcemods.securitycraft.fluids;

import java.util.Optional;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Lava that is not lava: it looks and flows exactly like it, but it puts fires out and heals whoever stands in
 * it (see {@link net.geforcemods.securitycraft.blocks.FakeLavaBlock}).
 *
 * <p>Upstream builds this out of NeoForge's {@code ForgeFlowingFluid}, which Fabric has no counterpart for, so
 * the flow behaviour is spelled out here the way vanilla's own {@code LavaFluid} does it.
 */
public abstract class FakeLavaFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return SCContent.FLOWING_FAKE_LAVA;
	}

	@Override
	public Fluid getSource() {
		return SCContent.FAKE_LAVA;
	}

	@Override
	public Item getBucket() {
		return SCContent.FAKE_LAVA_BUCKET;
	}

	@Override
	protected boolean canConvertToSource(net.minecraft.server.level.ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity be = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;

		Block.dropResources(state, level, pos, be);
	}

	@Override
	protected int getSlopeFindDistance(LevelReader level) {
		return 2;
	}

	@Override
	protected int getDropOff(LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 30;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return SCContent.FAKE_LAVA_BLOCK.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == SCContent.FAKE_LAVA || fluid == SCContent.FLOWING_FAKE_LAVA;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !isSame(fluid);
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
	}

	public static class Flowing extends FakeLavaFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Source extends FakeLavaFluid {
		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}
}
