package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Reinforced iron trapdoor: opens only when a powered SecurityCraft keypad is adjacent — NOT by regular redstone. */
public class ReinforcedTrapdoorBlock extends TrapDoorBlock {
	public ReinforcedTrapdoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, properties);
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
