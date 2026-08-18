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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Reinforced iron trapdoor: opens only when a powered SecurityCraft keypad is adjacent — NOT by regular redstone. */
public class ReinforcedTrapdoorBlock extends TrapDoorBlock implements IReinforcedBlock, EntityBlock {
	private final float destroyTimeForOwner;

	public ReinforcedTrapdoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
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
		return OwnershipUtils.newBlockEntity(pos, state);
	}

	private static boolean hasActiveKeypadNextTo(Level level, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(dir));

			if (neighbor.getBlock() instanceof KeypadBlock && neighbor.getValue(BlockStateProperties.POWERED))
				return true;
		}

		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState base = super.getStateForPlacement(ctx);

		if (base == null)
			return null;

		boolean active = hasActiveKeypadNextTo(ctx.getLevel(), ctx.getClickedPos());
		return base.setValue(BlockStateProperties.OPEN, active).setValue(BlockStateProperties.POWERED, active);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		boolean active = hasActiveKeypadNextTo(level, pos);

		if (active != state.getValue(BlockStateProperties.OPEN))
			level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, active).setValue(BlockStateProperties.POWERED, active), 2);
	}
}
