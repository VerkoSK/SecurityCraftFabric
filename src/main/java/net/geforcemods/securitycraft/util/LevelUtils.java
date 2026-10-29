package net.geforcemods.securitycraft.util;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LevelUtils {
	private LevelUtils() {}

	public static void addScheduledTask(LevelAccessor level, Runnable runnable) {
		if (level.isClientSide())
			Minecraft.getInstance().execute(runnable);
		else if (SecurityCraft.SERVER != null)
			SecurityCraft.SERVER.execute(runnable);
	}

	public static boolean checkCoordinates(GlobalPos pos, String[] coordinates) {
		int xPos = Integer.parseInt(coordinates[0]);
		int yPos = Integer.parseInt(coordinates[1]);
		int zPos = Integer.parseInt(coordinates[2]);
		ResourceLocation dim = new ResourceLocation(coordinates.length == 4 ? coordinates[3] : "");

		return pos.pos().getX() == xPos && pos.pos().getY() == yPos && pos.pos().getZ() == zPos && pos.dimension().location().equals(dim);
	}

	public static String toNBTString(GlobalPos pos) {
		return pos.pos().getX() + " " + pos.pos().getY() + " " + pos.pos().getZ() + " " + pos.dimension().location();
	}

	public static LightningBolt createLightning(Level level, Vec3 pos, boolean effectOnly) {
		LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);

		lightning.moveTo(pos);
		lightning.setVisualOnly(effectOnly);
		return lightning;
	}

	public static <T extends BlockEntity> void blockEntityTicker(Level level, BlockPos pos, BlockState state, T be) {
		if (be instanceof ITickingBlockEntity ticking)
			ticking.tick(level, pos, state);
	}

	/** Public stand-in for {@code BaseEntityBlock.createTickerHelper}, which is protected and thus unreachable from blocks that do not extend it. */
	@SuppressWarnings("unchecked")
	public static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}
}
