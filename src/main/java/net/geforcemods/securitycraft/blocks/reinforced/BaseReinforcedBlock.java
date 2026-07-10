package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.minecraft.world.level.block.Block;

/**
 * A blast-resistant ("reinforced") version of a vanilla block. Reinforced blocks look identical to
 * their vanilla counterparts but are effectively immune to explosions — SecurityCraft's signature
 * defensive building material.
 */
public class BaseReinforcedBlock extends Block implements IReinforcedBlock {
	public BaseReinforcedBlock(Properties properties) {
		super(properties);
	}
}
