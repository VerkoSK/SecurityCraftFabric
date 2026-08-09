package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.mines.BrushableMineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 1:1 with the upstream {@code blockentities.BrushableMineBlockEntity}. Upstream reaches into
 * {@code BrushableBlockEntity}'s private state through Forge access transformers; this port does the same through
 * {@code securitycraft.accesswidener}. {@code brushingCompleted} is a plain method here rather than an override,
 * because the vanilla one stays private (it is only ever called from {@link #brush}, which is fully overridden), and
 * Forge's {@code onDataPacket}/{@code handleUpdateTag} pair is dropped since vanilla already routes both the update
 * packet and the chunk payload through {@link #load}.
 */
public class BrushableMineBlockEntity extends BrushableBlockEntity implements IOwnable {
	private Owner owner = new Owner();

	public BrushableMineBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public boolean brush(long tickCount, Player player, Direction direction) {
		if (hitDirection == null)
			hitDirection = direction;

		brushCountResetsAtTick = tickCount + 40L;

		if (tickCount >= coolDownEndsAtTick && level instanceof ServerLevel) {
			coolDownEndsAtTick = tickCount + 10L;
			unpackLootTable(player);

			int previousCompletionState = getCompletionState();

			if (++brushCount >= 10) {
				brushingCompleted(player);
				return true;
			}
			else {
				level.scheduleTick(getBlockPos(), getBlockState().getBlock(), 40);

				int newCompletionState = getCompletionState();

				if (previousCompletionState != newCompletionState) {
					if (newCompletionState > 1 && !getBlockState().getValue(BrushableMineBlock.SAFE) && !isOwnedBy(player))
						((BrushableMineBlock) getBlockState().getBlock()).explode(level, worldPosition);
					else
						level.setBlock(getBlockPos(), getBlockState().setValue(BlockStateProperties.DUSTED, newCompletionState), 3);
				}
			}
		}

		return false;
	}

	@Override
	public void checkReset() {
		if (level != null) {
			if (brushCount != 0 && level.getGameTime() >= brushCountResetsAtTick) {
				int previousCompletionState = getCompletionState();

				brushCount = Math.max(0, this.brushCount - 2);

				int currentCompletionState = this.getCompletionState();

				if (previousCompletionState != currentCompletionState)
					level.setBlock(getBlockPos(), getBlockState().setValue(BlockStateProperties.DUSTED, currentCompletionState), 3);

				brushCountResetsAtTick = this.level.getGameTime() + 4L;
			}

			if (brushCount == 0) {
				hitDirection = null;
				brushCountResetsAtTick = 0L;
				coolDownEndsAtTick = 0L;
			}
			else
				level.scheduleTick(getBlockPos(), getBlockState().getBlock(), (int) (brushCountResetsAtTick - level.getGameTime()));
		}
	}

	public void brushingCompleted(Player player) {
		if (level != null && level.getServer() != null) {
			Block turnInto = Blocks.AIR;

			dropContent(player);
			level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE, getBlockPos(), Block.getId(getBlockState()));

			if (getBlockState().getBlock() instanceof BrushableMineBlock brushableMineBlock)
				turnInto = brushableMineBlock.getTurnsInto();

			level.setBlock(worldPosition, turnInto.defaultBlockState(), 3);
		}
	}

	@Override
	public BlockEntityType<?> getType() {
		return SCContent.BRUSHABLE_MINE_BLOCK_ENTITY;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.saveAdditional(tag, lookupProvider);

		if (owner != null)
			owner.save(tag, needsValidation());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.loadAdditional(tag, lookupProvider);
		owner.load(tag);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String name, String uuid) {
		owner.set(name, uuid);
		setChanged();
	}
}
