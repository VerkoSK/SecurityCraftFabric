package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Reinforced iron door: opens only when a powered SecurityCraft keypad is adjacent, not from ordinary redstone,
 * and — like every reinforced block — can only be mined by whoever placed it. Mirrors the trapdoor's behaviour.
 */
public class ReinforcedDoorBlock extends DoorBlock implements IReinforcedBlock, EntityBlock {
	private final float destroyTimeForOwner;

	public ReinforcedDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	/** Checks both halves, so a keypad next to either one opens the whole door. */
	private static boolean hasActiveKeypadNextTo(Level level, BlockPos pos, BlockState state) {
		BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

		for (BlockPos checked : new BlockPos[] {
				pos, other
		}) {
			for (Direction dir : Direction.values()) {
				BlockState neighbor = level.getBlockState(checked.relative(dir));

				if (neighbor.getBlock() instanceof KeypadBlock && neighbor.getValue(BlockStateProperties.POWERED))
					return true;
			}
		}

		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState base = super.getStateForPlacement(ctx);

		if (base == null)
			return null;

		boolean active = hasActiveKeypadNextTo(ctx.getLevel(), ctx.getClickedPos(), base);

		return base.setValue(BlockStateProperties.OPEN, active).setValue(BlockStateProperties.POWERED, active);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
		boolean active = hasActiveKeypadNextTo(level, pos, state);

		if (active != state.getValue(BlockStateProperties.OPEN)) {
			level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, active).setValue(BlockStateProperties.POWERED, active), 2);
			//setBlock alone is silent; vanilla doors play this from setOpen, which the keypad path bypasses
			level.playSound(null, pos, active ? type().doorOpen() : type().doorClose(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(null, active ? net.minecraft.world.level.gameevent.GameEvent.BLOCK_OPEN : net.minecraft.world.level.gameevent.GameEvent.BLOCK_CLOSE, pos);
		}
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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		//only the lower half carries the owner, so the upper half does not fight it for the same data
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? OwnershipUtils.newBlockEntity(pos, state) : null;
	}
}
