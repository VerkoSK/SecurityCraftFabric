package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedObserverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stores just the owner for a reinforced observer - no name, no GUI, no modules. Upstream hangs this off
 * {@code DisguisableBlockEntity} (for its generic module support, used here only for the disguise module),
 * which isn't ported; building the whole module-inventory machinery just to carry one module that nothing
 * else about this block needs would be more invention than port, so it's dropped along with the rest of the
 * disguise support (see {@link ReinforcedObserverBlock}).
 */
public class ReinforcedObserverBlockEntity extends OwnableBlockEntity {
	public ReinforcedObserverBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.REINFORCED_OBSERVER_BLOCK_ENTITY, pos, state);
	}

	//mirrors upstream's onOwnerChanged: reset POWERED to false whenever ownership changes, so a re-owned
	//observer doesn't keep pulsing for whatever the previous owner last validated
	@Override
	public void setOwner(String name, String uuid) {
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();

			if (state.hasProperty(ReinforcedObserverBlock.POWERED))
				level.setBlockAndUpdate(worldPosition, state.setValue(ReinforcedObserverBlock.POWERED, false));
		}

		super.setOwner(name, uuid);
	}

	@Override
	public boolean needsValidation() {
		return true;
	}
}
