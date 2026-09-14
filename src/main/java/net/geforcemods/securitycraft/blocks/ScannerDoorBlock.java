package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.blockentities.ScannerDoorBlockEntity;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Reinforced Iron Door that opens only when its owner (or an allowlisted player) looks at its lower half from close
 * range. Redstone cannot open it, and only whoever placed it can mine it. The block entity lives on the lower half.
 */
public class ScannerDoorBlock extends DoorBlock implements EntityBlock {
	private final float destroyTimeForOwner;

	public ScannerDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), type);
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	public static Direction.Axis getFacingAxis(BlockState state) {
		Direction facing = state.getValue(DoorBlock.FACING);

		return state.getValue(DoorBlock.OPEN) ? facing.getClockWise().getAxis() : facing.getAxis();
	}

	/** The scanner door reacts to being looked at, not to being right-clicked. */
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	/** Toggles both halves of the door at {@code lowerPos} and plays the door sound, bypassing redstone. */
	public void activate(Level level, BlockPos lowerPos) {
		BlockState lower = level.getBlockState(lowerPos);

		if (!(lower.getBlock() instanceof ScannerDoorBlock))
			return;

		boolean open = !lower.getValue(OPEN);

		level.setBlock(lowerPos, lower.setValue(OPEN, open), 2);

		BlockState upper = level.getBlockState(lowerPos.above());

		if (upper.getBlock() instanceof ScannerDoorBlock)
			level.setBlock(lowerPos.above(), upper.setValue(OPEN, open), 2);

		level.playSound(null, lowerPos, open ? type().doorOpen() : type().doorClose(), SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, lowerPos);
		level.updateNeighborsAt(lowerPos, this);
	}

	/** A redstone signal must not open the scanner door the way it opens a vanilla one. */
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {}

	/** Placing next to a redstone source must not place the door already open. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);

		return state == null ? null : state.setValue(OPEN, false).setValue(POWERED, false);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		OwnershipUtils.setPlacedBy(level, pos, placer);
		OwnershipUtils.setPlacedBy(level, pos.above(), placer);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.LOWER && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.getInventory().clear();

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.dropAllModules();

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ScannerDoorBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide || state.getValue(HALF) != DoubleBlockHalf.LOWER ? null : LevelUtils.createTickerHelper(type, net.geforcemods.securitycraft.SCContent.SCANNER_DOOR_BLOCK_ENTITY, LevelUtils::blockEntityTicker);
	}
}
