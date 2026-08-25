package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedDispenserBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The reinforced counterpart of vanilla's dispenser. {@code dispenseFrom} is inherited unchanged from
 * {@link DispenserBlock} - it reads the block entity through {@code BlockSourceImpl#getEntity()}, which
 * resolves generically, so it keeps dispensing correctly once that block entity is a
 * {@link ReinforcedDispenserBlockEntity}. Upstream reimplements it only to route through Forge's
 * {@code VanillaInventoryCodeHooks}, which doesn't exist on Fabric; without it, vanilla's own method already
 * does exactly what upstream's override did.
 *
 * <p>The waterlogging support here (the {@code WATERLOGGED} property, {@link SimpleWaterloggedBlock}) is
 * genuinely upstream's own addition to the reinforced dispenser (vanilla's dispenser can't be waterlogged),
 * not something pulled in from elsewhere, so it is ported as-is.
 *
 * <p>Dropped versus upstream: the {@code IDisguisable}-driven appearance/shape/light/sound overrides and
 * {@code IOverlayDisplay} (same reasoning as the reinforced hopper - disguise support is limited to the
 * block entity's render data); and the {@code alwaysDrop} config check in {@code canHarvestBlock} (not
 * ported, so vanilla's own tool-based harvesting rule applies).
 */
public class ReinforcedDispenserBlock extends DispenserBlock implements IReinforcedBlock, SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private final float destroyTimeForOwner;

	public ReinforcedDispenserBlock(BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		//only allow the owner or players on the allowlist to access a reinforced dispenser
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof ReinforcedDispenserBlockEntity be && (be.isOwnedBy(player) || be.isAllowed(player)))
			player.openMenu(be);

		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ReinforcedDispenserBlockEntity be) {
			if (isMoving)
				be.clearContent(); //clear the items from the block before it is moved by a piston to prevent duplication

			level.updateNeighbourForOutputSignal(pos, this);
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedDispenserBlockEntity(pos, state);
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.DISPENSER;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx).setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}
}
