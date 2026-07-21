package net.geforcemods.securitycraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines a block that can be converted to a passcode-protected variant by right-clicking it with a Key Panel.
 * Register implementations via {@link SecurityCraftAPI#registerPasscodeConvertible}.
 */
public interface IPasscodeConvertible {
	/** @return true if the given state can be converted to a passcode-protected block by this implementation. */
	boolean isUnprotectedBlock(BlockState state);

	/** @return true if the given state is a passcode-protected block that can be converted back by this implementation. */
	boolean isProtectedBlock(BlockState state);

	/** Converts the original block to the passcode-protected one. @return true on success. */
	boolean protect(Player player, Level level, BlockPos pos);

	/** Converts the passcode-protected block back to the original one. @return true on success. */
	boolean unprotect(Player player, Level level, BlockPos pos);

	/** @return how many key panel items are consumed when converting the given state. */
	default int getRequiredKeyPanels(BlockState state) {
		return 1;
	}
}
