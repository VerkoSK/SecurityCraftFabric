package net.geforcemods.securitycraft.util;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The ownership behaviour shared by every owned block.
 *
 * <p>Upstream puts this on a common {@code OwnableBlock} superclass, which forces each reinforced shape to
 * reimplement the whole vanilla block it mirrors — its stairs class alone is close to 300 lines. Java allows
 * only one superclass, so this port keeps the vanilla shape as the superclass and pulls the ownership bits in
 * from here instead, which leaves the vanilla logic untouched and each reinforced shape a few lines long.
 */
public final class OwnershipUtils {
	private OwnershipUtils() {}

	/**
	 * Hands the real destroy time back to whoever is allowed to mine the block. Owned blocks are registered with
	 * a destroy time of -1, so vanilla would report them as unbreakable for everybody.
	 *
	 * <p>Upstream reaches into the block state and swaps its {@code destroySpeed} around the vanilla call. Block
	 * states are shared, immutable and read from several threads, so this instead reproduces vanilla's own
	 * formula against the stored destroy time, which needs no mutation.
	 */
	public static float getDestroyProgress(float destroyTimeForOwner, BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (destroyTimeForOwner == -1.0F)
			return 0.0F;

		boolean owned = !(level.getBlockEntity(pos) instanceof IOwnable ownable) || !ownable.getOwner().owns() || ownable.isOwnedBy(player);

		if (!owned && !ConfigHandler.allowBreakingNonOwnedBlocks)
			return 0.0F;

		int toolFactor = player.hasCorrectToolForDrops(state) ? 30 : 100;
		float progress = player.getDestroySpeed(state) / destroyTimeForOwner / toolFactor;
		double slowdown = owned ? ConfigHandler.ownedBreakingSlowdown : ConfigHandler.nonOwnedBreakingSlowdown;

		return progress / (float) Math.max(1.0, slowdown);
	}

	/** As above, but {@code ignoreOwnership} lets block mines stay breakable by trespassers so they can detonate. */
	public static float getDestroyProgress(float destroyTimeForOwner, BlockState state, Player player, BlockGetter level, BlockPos pos, boolean ignoreOwnership) {
		if (destroyTimeForOwner == -1.0F)
			return 0.0F;

		if (!ignoreOwnership)
			return getDestroyProgress(destroyTimeForOwner, state, player, level, pos);

		int toolFactor = player.hasCorrectToolForDrops(state) ? 30 : 100;

		return player.getDestroySpeed(state) / destroyTimeForOwner / toolFactor;
	}

	/** Makes the placing player the owner of the block that was just placed. */
	public static void setPlacedBy(Level level, BlockPos pos, LivingEntity placer) {
		if (placer instanceof Player player && level.getBlockEntity(pos) instanceof IOwnable ownable)
			ownable.setOwner(player.getGameProfile().getName(), player.getUUID().toString());
	}

	/** The block entity every owned block carries, which is what actually stores the owner. */
	public static BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}
}
