package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the laser block. Remembers its owner and a few options that control the laser's
 * behaviour. The full SecurityCraft laser additionally supports modules and multi-laser linking;
 * those are layered on top of this functional core.
 */
public class LaserBlockBlockEntity extends OwnableBlockEntity {
	private boolean disabled = false;
	private boolean ignoreOwner = true;
	private int signalLength = 50;
	private long lastToggleTime;

	public LaserBlockBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.LASER_BLOCK_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putBoolean("disabled", disabled);
		tag.putBoolean("ignoreOwner", ignoreOwner);
		tag.putInt("signalLength", signalLength);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		disabled = tag.getBoolean("disabled");
		ignoreOwner = tag.contains("ignoreOwner") ? tag.getBoolean("ignoreOwner") : true;
		signalLength = tag.contains("signalLength") ? tag.getInt("signalLength") : 50;
	}

	public boolean isEnabled() {
		return !disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		setChanged();
		sync();
	}

	public boolean ignoresOwner() {
		return ignoreOwner;
	}

	public void setIgnoreOwner(boolean ignoreOwner) {
		this.ignoreOwner = ignoreOwner;
		setChanged();
		sync();
	}

	public int getSignalLength() {
		return signalLength;
	}

	public void setSignalLength(int signalLength) {
		this.signalLength = signalLength;
		setChanged();
		sync();
	}

	public long getLastToggleTime() {
		return lastToggleTime;
	}

	public void setLastToggleTime(long lastToggleTime) {
		this.lastToggleTime = lastToggleTime;
	}

	public long timeSinceLastToggle() {
		return System.currentTimeMillis() - lastToggleTime;
	}
}
