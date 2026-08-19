package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedHopperBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The reinforced counterpart of vanilla's hopper. Everything not touching ownership (shape, placement,
 * neighbor signal handling, item movement) is inherited straight from {@link HopperBlock} - it already
 * resolves {@code level.getBlockEntity(pos)} generically, so it keeps working once that block entity is a
 * {@link ReinforcedHopperBlockEntity} instead of a plain {@link HopperBlockEntity}.
 *
 * <p>Dropped versus upstream: the {@code IDisguisable}-driven appearance/shape/light/sound overrides and
 * {@code IOverlayDisplay} (no disguise-at-the-block-level or Jade/WAILA equivalent in this port - disguise
 * support here is limited to the block entity's render data, same as {@code KeypadBlockEntity}); the
 * {@code alwaysDrop} config check in {@code canHarvestBlock} (that option isn't ported, so vanilla's own
 * tool-based harvesting rule applies); and the nested {@code ExtractionBlock}, which implemented the
 * (unported) {@code IExtractionBlock} to gate hopper-to-hopper extraction.
 */
public class ReinforcedHopperBlock extends HopperBlock implements IReinforcedBlock {
	private final float destroyTimeForOwner;

	public ReinforcedHopperBlock(BlockBehaviour.Properties properties) {
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

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		//only allow the owner or players on the allowlist to access a reinforced hopper
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof ReinforcedHopperBlockEntity be && (be.isOwnedBy(player) || be.isAllowed(player)))
			player.openMenu(be);

		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ReinforcedHopperBlockEntity be) {
			if (isMoving)
				be.clearContent(); //clear the items from the block before it is moved by a piston to prevent duplication

			level.updateNeighbourForOutputSignal(pos, this);
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedHopperBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, SCContent.REINFORCED_HOPPER_BLOCK_ENTITY, ReinforcedHopperBlockEntity::pushItemsTick);
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.HOPPER;
	}
}
