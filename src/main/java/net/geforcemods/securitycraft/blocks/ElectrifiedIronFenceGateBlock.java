package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.blockentities.ElectrifiedFenceAndGateBlockEntity;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The electrified iron fence gate: an unbreakable iron gate that shocks anyone but its owner and the players on
 * its allowlist, and that cannot be opened by hand - only redstone moves it. 1:1 with upstream's block of the
 * same name; upstream's registry name for it is {@code reinforced_fence_gate}, which is kept here.
 */
public class ElectrifiedIronFenceGateBlock extends FenceGateBlock implements EntityBlock {
	private final float destroyTimeForOwner;

	public ElectrifiedIronFenceGateBlock(BlockBehaviour.Properties properties) {
		//upstream passes the iron door sounds to Forge's (properties, openSound, closeSound) constructor, which
		//vanilla does not have; the wood type given here is never heard, because the only two places that would
		//play its sounds are use (blocked) and neighborChanged (overridden below with the iron door sounds)
		super(OwnableBlock.withReinforcedDestroyTime(properties), WoodType.OAK);
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide) {
			boolean powered = level.hasNeighborSignal(pos);

			if (state.getValue(POWERED) != powered) {
				level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, powered), 2);

				if (state.getValue(OPEN) != powered) {
					level.playSound(null, pos, powered ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
					level.gameEvent(null, powered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
				}
			}
		}
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
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.FAIL;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean stillInside) {
		if (state.getValue(OPEN))
			return;

		ElectrifiedIronFenceBlock.hurtOrConvertEntity(this, state, level, pos, entity);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		BlockEntity be = level.getBlockEntity(pos);

		return be != null && be.triggerEvent(id, param);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ElectrifiedFenceAndGateBlockEntity(pos, state);
	}
}
