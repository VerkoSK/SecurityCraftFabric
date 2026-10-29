package net.geforcemods.securitycraft.entity.sentry;

import java.util.EnumSet;
import java.util.function.Supplier;

import net.geforcemods.securitycraft.entity.sentry.Sentry.SentryMode;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class AttackRangedIfEnabledGoal extends Goal {
	private Sentry sentry;
	private final RangedAttackMob rangedAttackEntityHost;
	private LivingEntity attackTarget;
	private int rangedAttackTime;
	private final Supplier<Integer> maxAttackTime;
	private final float attackRadius;

	public AttackRangedIfEnabledGoal(RangedAttackMob attacker, Supplier<Integer> maxAttackTime, float maxAttackDistanceIn) {
		sentry = (Sentry) attacker;
		rangedAttackEntityHost = attacker;
		rangedAttackTime = -1;
		this.maxAttackTime = maxAttackTime;
		attackRadius = maxAttackDistanceIn;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity potentialTarget = sentry.getTarget();

		if (potentialTarget == null)
			return false;
		else if (sentry.isShutDown()) {
			sentry.setTarget(null);
			return false;
		}
		else {
			attackTarget = potentialTarget;
			return sentry.getMode() != SentryMode.IDLE;
		}
	}

	@Override
	public void stop() {
		attackTarget = null;
		rangedAttackTime = -3;
	}

	@Override
	public void tick() {
		double distanceToTarget = sentry.distanceToSqr(attackTarget.getX(), attackTarget.getBoundingBox().minY, attackTarget.getZ());
		boolean canSee = sentry.getSensing().hasLineOfSight(attackTarget);

		if (!canSee) {
			sentry.setTarget(null);
			return;
		}

		if (--rangedAttackTime == 0) {
			float f = Mth.sqrt((float) distanceToTarget) / attackRadius;
			float lvt_5_1_ = Mth.clamp(f, 0.1F, 1.0F);

			rangedAttackEntityHost.performRangedAttack(attackTarget, lvt_5_1_);
			rangedAttackTime = maxAttackTime.get();
		}
		else if (rangedAttackTime < 0)
			rangedAttackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceToTarget) / attackRadius, maxAttackTime.get(), maxAttackTime.get()));
	}
}
