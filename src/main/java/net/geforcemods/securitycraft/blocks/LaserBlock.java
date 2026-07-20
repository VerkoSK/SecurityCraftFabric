package net.geforcemods.securitycraft.blocks;

import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The laser emitter. On placement it shoots laser fields ({@link LaserFieldBlock}) toward any
 * owner-matching laser block within range, filling the gap with beam segments. Entities passing
 * through a beam take damage and briefly power this block (redstone output).
 */
public class LaserBlock extends BaseEntityBlock {
	public static final MapCodec<LaserBlock> CODEC = simpleCodec(LaserBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	/** At most how many blocks away a laser block connects to another laser block. */
	public static final int RANGE = 5;
	/** Damage (in half-hearts) inflicted to an entity passing through a beam. */
	public static final double DAMAGE = 10.0;

	public LaserBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(POWERED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (!level.isClientSide()) {
			if (entity instanceof Player player && level.getBlockEntity(pos) instanceof IOwnable ownable)
				ownable.setOwner(player.getGameProfile().getName(), player.getUUID().toString());

			setLaser(level, pos);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof LaserBlockBlockEntity be && be.isOwnedBy(player)) {
			if (!level.isClientSide()) {
				be.setDisabled(!be.isEnabled());

				if (be.isEnabled())
					setLaser(level, pos);
				else
					destroyAdjacentLasers(level, pos);

				player.displayClientMessage(Utils.localize(be.isEnabled() ? "messages.securitycraft:laser.enabled" : "messages.securitycraft:laser.disabled"), true);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	public void setLaser(Level level, BlockPos pos) {
		if (!(level.getBlockEntity(pos) instanceof LaserBlockBlockEntity thisBe) || !thisBe.isEnabled())
			return;

		for (Direction facing : Direction.values()) {
			int boundType = LaserFieldBlock.getBoundType(facing);

			for (int i = 1; i <= RANGE; i++) {
				BlockPos offsetPos = pos.relative(facing, i);
				BlockState offsetState = level.getBlockState(offsetPos);
				Block offsetBlock = offsetState.getBlock();

				if (offsetBlock == SCContent.LASER_BLOCK) {
					if (level.getBlockEntity(offsetPos) instanceof LaserBlockBlockEntity thatBe && thatBe.isEnabled() && sameOwner(thisBe, thatBe)) {
						for (int j = 1; j < i; j++) {
							BlockPos fieldPos = pos.relative(facing, j);
							BlockState fieldState = level.getBlockState(fieldPos);

							if (fieldState.isAir() || fieldState.canBeReplaced()) {
								level.setBlockAndUpdate(fieldPos, SCContent.LASER_FIELD.getPotentiallyWaterloggedState(boundType, level, fieldPos));

								if (level.getBlockEntity(fieldPos) instanceof IOwnable ownable)
									ownable.setOwner(thisBe.getOwner().getName(), thisBe.getOwner().getUUID());
							}
						}
					}

					break;
				}
				else if (!offsetState.isAir() && !offsetState.canBeReplaced() && offsetBlock != SCContent.LASER_FIELD)
					break;
			}
		}
	}

	private static boolean sameOwner(LaserBlockBlockEntity a, LaserBlockBlockEntity b) {
		return a.getOwner().owns() && a.getOwner().getUUID().equals(b.getOwner().getUUID());
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (!level.isClientSide())
			destroyAdjacentLasers(level, pos);
	}

	public static void destroyAdjacentLasers(LevelAccessor level, BlockPos pos) {
		BlockUtils.removeInSequence((direction, state) -> state.getBlock() == SCContent.LASER_FIELD && state.getValue(LaserFieldBlock.BOUNDTYPE) == LaserFieldBlock.getBoundType(direction), level, pos, Direction.values());
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide())
			setLaser(level, pos);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return getSignal(state, level, pos, side);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED)) {
			level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, pos, SCContent.LASER_BLOCK);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (state.getValue(POWERED)) {
			double x = pos.getX() + 0.5F + (rand.nextFloat() - 0.5F) * 0.2D;
			double y = pos.getY() + 0.7F + (rand.nextFloat() - 0.5F) * 0.2D;
			double z = pos.getZ() + 0.5F + (rand.nextFloat() - 0.5F) * 0.2D;
			double offset = 0.27000001072883606D;
			double height = 0.2199999988079071D;
			Vector3f color = new Vector3f(1.0F, 0.0F, 0.0F);

			level.addParticle(new DustParticleOptions(color, 1), false, x - offset, y + height, z, 0.0D, 0.0D, 0.0D);
			level.addParticle(new DustParticleOptions(color, 1), false, x + offset, y + height, z, 0.0D, 0.0D, 0.0D);
			level.addParticle(new DustParticleOptions(color, 1), false, x, y + height, z - offset, 0.0D, 0.0D, 0.0D);
			level.addParticle(new DustParticleOptions(color, 1), false, x, y + height, z + offset, 0.0D, 0.0D, 0.0D);
			level.addParticle(new DustParticleOptions(color, 1), false, x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LaserBlockBlockEntity(pos, state);
	}
}
