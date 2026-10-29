package net.geforcemods.securitycraft.blockentities;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.api.Option.TargetingModeOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.SometimesVisibleBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.TargetingMode;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FloorTrapBlockEntity extends DisguisableBlockEntity implements ITickingBlockEntity {
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);
	private BooleanOption disappearInstantlyInChains = new BooleanOption("disappearInstantlyInChains", true);
	private IntOption disappearDelay = new IntOption("disappearDelay", 5, 0, 200, 1);
	private IntOption reappearDelay = new IntOption("reappearDelay", 20, 5, 200, 1);
	private TargetingModeOption targetingMode = new TargetingModeOption(TargetingMode.PLAYERS);
	private DisabledOption disabled = new DisabledOption(false);
	private boolean shouldDisappear, shouldReappear;
	private int disappearTicks = -1, reappearTicks = -1;

	public FloorTrapBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.FLOOR_TRAP_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) {
			if (shouldReappear && reappearTicks-- <= 0)
				shouldReappear = false;

			return;
		}

		if (isDisabled())
			return;

		if (shouldReappear) {
			if (reappearTicks-- <= 0)
				reappear();

			return;
		}

		if (shouldDisappear) {
			if (disappearTicks-- <= 0)
				disappear();

			return;
		}

		TargetingMode mode = getTargetingMode();
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.above()).setMaxY(pos.getY() + 1.1666D), e -> mode.canAttackEntity(e, this, entity -> false));

		if (!entities.isEmpty())
			scheduleDisappear();
	}

	public boolean onlyAllowsOwnedEntities(LivingEntity entity) {
		if (entity instanceof OwnableEntity ownableEntity) {
			Owner otherOwner = new Owner();
			LivingEntity owner = ownableEntity.getOwner();

			if (owner != null)
				otherOwner.set(owner.getName().getString(), owner.getUUID().toString());
			else if (ownableEntity.getOwnerUUID() != null)
				otherOwner.set("owner", ownableEntity.getOwnerUUID().toString());

			return ignoresOwner() && isOwnedBy(otherOwner) || !ignoresOwner() && isAllowed(otherOwner.getName());
		}

		return false;
	}

	public void scheduleDisappear() {
		scheduleDisappear(disappearDelay.get(), false);
	}

	public void scheduleDisappear(int delay, boolean force) {
		if (shouldDisappear && !force)
			return;

		disappearTicks = delay;
		shouldDisappear = true;
	}

	public void disappear() {
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SometimesVisibleBlock.INVISIBLE, true));
		level.playSound(null, worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
		shouldDisappear = false;
		scheduleReappear(reappearDelay.get());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
	}

	public void scheduleReappear(int delay) {
		if (shouldReappear)
			return;

		reappearTicks = delay;
		shouldReappear = true;
	}

	public void reappear() {
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SometimesVisibleBlock.INVISIBLE, false));
		shouldReappear = false;
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("should_reappear", shouldReappear);
		tag.putInt("reappear_ticks", reappearTicks);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		shouldReappear = tag.getBoolean("should_reappear");
		reappearTicks = tag.getInt("reappear_ticks");

		if (level != null && level.isClientSide && shouldReappear && getBlockState().getValue(SometimesVisibleBlock.INVISIBLE))
			spawnParticles();
	}

	private void spawnParticles() {
		level.addParticle(SCContent.FLOOR_TRAP_CLOUD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DISGUISE, ModuleType.ALLOWLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				ignoreOwner, disappearInstantlyInChains, disappearDelay, reappearDelay, targetingMode, disabled
		};
	}

	@Override
	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	public boolean shouldDisappearInstantlyInChains() {
		return disappearInstantlyInChains.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public TargetingMode getTargetingMode() {
		return targetingMode.get();
	}
}
