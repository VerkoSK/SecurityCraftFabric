package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.minecraft.block.Block;

/** A blast-resistant (reinforced) version of a vanilla block. */
public class BaseReinforcedBlock extends Block implements IReinforcedBlock {
	public BaseReinforcedBlock(Settings settings) {
		super(settings);
	}
}
