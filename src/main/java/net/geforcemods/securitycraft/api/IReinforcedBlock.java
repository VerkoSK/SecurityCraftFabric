package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * A reinforced ("blast-proof") counterpart of a vanilla block. Reinforced blocks look identical to the block
 * they mirror, resist explosions, and can only be broken by whoever placed them.
 */
public interface IReinforcedBlock {
	/** The vanilla block this one mirrors. Derived from the registry name, which is always {@code reinforced_<vanilla>}. */
	default Block getVanillaBlock() {
		return SCContent.vanillaCounterpart((Block) this);
	}

	/** Carries every shared block state property across when a vanilla block is reinforced. */
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	default BlockState convertToReinforced(BlockState vanillaState) {
		BlockState state = ((Block) this).defaultBlockState();

		for (Property property : vanillaState.getProperties()) {
			if (state.hasProperty(property))
				state = state.setValue(property, vanillaState.getValue(property));
		}

		return state;
	}

	/** The reverse of {@link #convertToReinforced}, used when a reinforced block is turned back. */
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	default BlockState convertToVanilla(BlockState reinforcedState) {
		Block vanilla = getVanillaBlock();

		if (vanilla == null)
			return reinforcedState;

		BlockState state = vanilla.defaultBlockState();

		for (Property property : reinforcedState.getProperties()) {
			if (state.hasProperty(property))
				state = state.setValue(property, reinforcedState.getValue(property));
		}

		return state;
	}
}
