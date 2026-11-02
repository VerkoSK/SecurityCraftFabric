package net.geforcemods.securitycraft.blockentities;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.api.Option.RespectInvisibilityOption;
import net.geforcemods.securitycraft.blocks.MotionActivatedLightBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MotionActivatedLightBlockEntity extends CustomizableBlockEntity implements ITickingBlockEntity {
	private static final int TICKS_BETWEEN_ATTACKS = 5;
	private IntOption searchRadiusOption = new IntOption("searchRadius", 5, 5, 20, 1);
	private DisabledOption disabled = new DisabledOption(false);
	private RespectInvisibilityOption respectInvisibility = new RespectInvisibilityOption();
	private int cooldown = TICKS_BETWEEN_ATTACKS;

	public MotionActivatedLightBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.MOTION_LIGHT_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (isDisabled() || cooldown-- > 0)
			return;

		Direction facing = state.getValue(MotionActivatedLightBlock.FACING);
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(searchRadiusOption.get()), e -> !respectInvisibility.isConsideredInvisible(e) && e.canBeSeenByAnyone() && !(e instanceof ArmorStand) && canLightSeeEntity(level, pos, facing, e));
		boolean shouldBeOn = !entities.isEmpty();

		if (state.getValue(MotionActivatedLightBlock.LIT) != shouldBeOn)
			level.setBlockAndUpdate(pos, state.setValue(MotionActivatedLightBlock.LIT, shouldBeOn));

		cooldown = TICKS_BETWEEN_ATTACKS;
	}

	private boolean canLightSeeEntity(Level level, BlockPos pos, Direction facing, LivingEntity entity) {
		Vec3 sensorPos = Vec3.atCenterOf(pos).add(facing.getStepX() * 0.25, facing.getStepY() * 0.25, facing.getStepZ() * 0.25);

		// Don't detect entities that are behind the wall the light is mounted on
		Vec3 toEye = entity.getEyePosition().subtract(sensorPos);
		double dot = toEye.x * facing.getStepX() + toEye.y * facing.getStepY() + toEye.z * facing.getStepZ();
		if (dot < -0.15)
			return false;

		// Check line of sight to eye position
		BlockHitResult eyeHit = level.clip(new ClipContext(sensorPos, entity.getEyePosition(), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
		if (eyeHit.getType() == HitResult.Type.MISS || eyeHit.getBlockPos().equals(pos))
			return true;

		// Check line of sight to body position
		Vec3 bodyPos = entity.position().add(0, 0.5, 0);
		BlockHitResult bodyHit = level.clip(new ClipContext(sensorPos, bodyPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
		return bodyHit.getType() == HitResult.Type.MISS || bodyHit.getBlockPos().equals(pos);
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		//turn off the light when it's disabled
		if (option == disabled && ((BooleanOption) option).get() && getBlockState().getValue(MotionActivatedLightBlock.LIT))
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(MotionActivatedLightBlock.LIT, false));

		super.onOptionChanged(option);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option<?>[] {
				searchRadiusOption, disabled, respectInvisibility
		};
	}

	public boolean isDisabled() {
		return disabled.get();
	}
}
