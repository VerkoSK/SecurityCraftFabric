package net.geforcemods.securitycraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LevelUtils {
	private LevelUtils() {}

	public static <T extends BlockEntity> void blockEntityTicker(Level level, BlockPos pos, BlockState state, T be) {
		if (be instanceof ITickingBlockEntity ticking)
			ticking.tick(level, pos, state);
	}

	/** Builds a {@link LightningBolt} the way upstream's helper does: created for {@code level}, snapped to {@code pos}. */
	public static LightningBolt createLightning(Level level, Vec3 pos, boolean effectOnly) {
		LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);

		lightning.snapTo(pos);
		lightning.setVisualOnly(effectOnly);
		return lightning;
	}

	/** Runs {@code runnable} on the client's or the server's main thread, whichever owns {@code level}. */
	public static void addScheduledTask(LevelAccessor level, Runnable runnable) {
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getServer().execute(runnable);
		else if (level.isClientSide())
			net.minecraft.client.Minecraft.getInstance().execute(runnable);
		else
			runnable.run();
	}
}
