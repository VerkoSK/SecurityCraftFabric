package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 1:1 with the upstream {@code blockentities.CreakingHeartMineBlockEntity}, minus the passcode-filtering
 * {@code getUpdateTag} override (this port has no passcode data on this block entity to filter out).
 */
public class CreakingHeartMineBlockEntity extends CreakingHeartBlockEntity implements IOwnable {
	private Owner owner = new Owner();

	public CreakingHeartMineBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		owner.save(tag, needsValidation());
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		owner = Owner.load(tag);
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

	@Override
	public BlockEntityType<?> getType() {
		return SCContent.CREAKING_HEART_MINE_BLOCK_ENTITY;
	}
}
