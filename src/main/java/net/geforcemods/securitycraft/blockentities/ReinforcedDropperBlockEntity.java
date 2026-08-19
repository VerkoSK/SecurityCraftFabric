package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The reinforced counterpart of vanilla's dropper block entity - just the dispenser one under its own type. */
public class ReinforcedDropperBlockEntity extends ReinforcedDispenserBlockEntity {
	public ReinforcedDropperBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.REINFORCED_DROPPER_BLOCK_ENTITY, pos, state);
	}
}
