package net.geforcemods.securitycraft.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/** Looks exactly like lava, but puts fires out and heals whoever stands in it. 1:1 with upstream's block. */
public class FakeLavaBlock extends LiquidBlock {
	private static final MobEffectInstance SHORT_FIRE_RESISTANCE = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1);

	public FakeLavaBlock(BlockBehaviour.Properties properties, Supplier<? extends FlowingFluid> fluid) {
		super(fluid.get(), properties);
	}

	@Override
	public net.minecraft.network.chat.MutableComponent getName() {
		//the whole point is that it cannot be told apart from real lava, so it borrows lava's name everywhere it shows (JEI, tooltips)
		return net.minecraft.world.level.block.Blocks.LAVA.getName();
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
		super.entityInside(state, level, pos, entity, effectApplier);

		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.clearFire();
			livingEntity.setSharedFlagOnFire(false);

			if (!level.isClientSide) {
				livingEntity.addEffect(SHORT_FIRE_RESISTANCE);

				if (!livingEntity.hasEffect(MobEffects.REGENERATION))
					livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 2, false, false));
			}
		}
	}
}
