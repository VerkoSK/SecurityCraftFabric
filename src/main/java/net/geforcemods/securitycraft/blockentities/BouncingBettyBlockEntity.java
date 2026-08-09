package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1:1 with the upstream {@code blockentities.BouncingBettyBlockEntity}. Upstream rides on the shared, state-dispatching
 * {@code ABSTRACT_BLOCK_ENTITY} type; this port follows its own one-type-per-block-entity convention instead.
 */
public class BouncingBettyBlockEntity extends MineBlockEntity {
	public BouncingBettyBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.BOUNCING_BETTY_BLOCK_ENTITY, pos, state);
	}
}
