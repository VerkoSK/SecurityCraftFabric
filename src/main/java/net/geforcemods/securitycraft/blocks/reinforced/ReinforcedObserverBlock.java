package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedObserverBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The reinforced counterpart of vanilla's observer. Rotation/mirroring, redstone signal output, the
 * facing-side placement and the placement/removal power desync fix are all inherited unchanged from
 * {@link ObserverBlock} - upstream duplicates that logic verbatim (it has to, since it hangs off
 * {@code DisguisableBlock} rather than {@code ObserverBlock}), but re-adding it here on top of the real
 * vanilla superclass would just schedule/reset the same tick twice. {@link #tick} is the one genuinely
 * different piece: it gates the pulse on {@link IOwnable#getOwner()} being validated, same as upstream.
 *
 * <p>Dropped versus upstream: extending {@code DisguisableBlock} for its {@code WATERLOGGED} property and
 * disguise appearance hooks - neither is ported (no {@code IDisguisable} equivalent for this block; disguise
 * support elsewhere in this port is limited to a block entity's render data, which the observer has no need
 * for since it carries no modules - see {@link ReinforcedObserverBlockEntity}).
 */
public class ReinforcedObserverBlock extends ObserverBlock implements IReinforcedBlock, EntityBlock {
	private final float destroyTimeForOwner;

	public ReinforcedObserverBlock(BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	//only pulse for an owner that has been validated, same restriction upstream applies
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED))
			level.setBlock(pos, state.setValue(POWERED, false), 2);
		else {
			if (level.getBlockEntity(pos) instanceof IOwnable ownable && ownable.getOwner().isValidated()) {
				level.setBlock(pos, state.setValue(POWERED, true), 2);
				level.scheduleTick(pos, this, 2);
			}
			else
				return;
		}

		updateNeighborsInFront(level, pos, state);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		level.removeBlockEntity(pos); //vanilla's ObserverBlock has no block entity to clean up, ours does

		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}

	//upstream also overrides Forge's canConnectRedstone; vanilla has no such hook, redstone connection is decided
	//by isSignalSource and getSignal, which are inherited from ObserverBlock unchanged

	@Override
	public Block getVanillaBlock() {
		return Blocks.OBSERVER;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedObserverBlockEntity(pos, state);
	}
}
