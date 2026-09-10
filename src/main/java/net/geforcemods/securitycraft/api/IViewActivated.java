package net.geforcemods.securitycraft.api;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Lets a block entity react to an entity looking straight at it from close range (server side only). */
public interface IViewActivated {
	default void checkView(Level level, BlockPos pos) {
		if (getViewCooldown() > 0) {
			setViewCooldown(getViewCooldown() - 1);
			return;
		}

		double maximumDistance = getMaximumDistance();
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(maximumDistance), e -> !e.isSpectator() && !isConsideredInvisible(e) && (!activatedOnlyByPlayer() || e instanceof Player));

		for (LivingEntity entity : entities) {
			double eyeHeight = entity.getEyeHeight();
			Vec3 lookVec = new Vec3(entity.getX() + (entity.getLookAngle().x * maximumDistance), (eyeHeight + entity.getY()) + (entity.getLookAngle().y * maximumDistance), entity.getZ() + (entity.getLookAngle().z * maximumDistance));
			BlockHitResult hitResult = level.clip(new ClipContext(new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ()), lookVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));

			if (hitResult != null && hitResult.getBlockPos().getX() == pos.getX() && hitResult.getBlockPos().getY() == pos.getY() && hitResult.getBlockPos().getZ() == pos.getZ() && onEntityViewed(entity, hitResult))
				setViewCooldown(getDefaultViewCooldown());
		}
	}

	default int getDefaultViewCooldown() {
		return 30;
	}

	int getViewCooldown();

	void setViewCooldown(int viewCooldown);

	/** @return true if the block entity's view cooldown should be reset (i.e. the look was handled) */
	boolean onEntityViewed(LivingEntity entity, BlockHitResult hitResult);

	default boolean activatedOnlyByPlayer() {
		return true;
	}

	double getMaximumDistance();

	boolean isConsideredInvisible(LivingEntity entity);
}
