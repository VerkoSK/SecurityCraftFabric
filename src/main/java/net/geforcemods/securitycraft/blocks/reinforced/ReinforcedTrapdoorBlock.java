package net.geforcemods.securitycraft.blocks.reinforced;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Reinforced iron trapdoor: ignores regular redstone (no neighbor-triggered open/close). */
public class ReinforcedTrapdoorBlock extends TrapDoorBlock {
	public ReinforcedTrapdoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(type, properties);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
		// Reinforced trapdoors are not controlled by regular redstone.
	}
}
