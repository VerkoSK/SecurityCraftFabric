package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * A blast-resistant ("reinforced") version of a vanilla block. Reinforced blocks look identical to their
 * vanilla counterparts, are effectively immune to explosions, and — through {@link OwnableBlock} — can only be
 * mined by the player who placed them.
 */
public class BaseReinforcedBlock extends OwnableBlock implements IReinforcedBlock {
	public BaseReinforcedBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
}
