package net.geforcemods.securitycraft.blocks.mines;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.api.IBlockMine;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mine disguised as a full vanilla block. 1:1 with the upstream {@code blocks.mines.BaseFullMineBlock}, minus the
 * NeoForge-only {@code getAppearance} and the Jade/WAILA {@code IOverlayDisplay} implementation (neither has a
 * Fabric counterpart in this port), and with {@code onDestroyedByPlayer}/the 5-arg {@code getCloneItemStack}
 * expressed through their vanilla equivalents.
 */
public class BaseFullMineBlock extends ExplosiveBlock implements IBlockMine {
	private final Block blockDisguisedAs;

	public BaseFullMineBlock(BlockBehaviour.Properties properties, Block disguisedBlock) {
		super(properties);
		blockDisguisedAs = disguisedBlock;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
		if (collisionContext instanceof EntityCollisionContext ctx && ctx.getEntity() != null) {
			Entity entity = ctx.getEntity();

			if ((entity instanceof ItemEntity) || level.getBlockEntity(pos) instanceof IOwnable ownable && ((entity instanceof Player player && (ownable.isOwnedBy(player) || player.isCreative())) || (entity instanceof OwnableEntity ownableEntity && ownable.allowsOwnableEntity(ownableEntity))))
				return Shapes.block();

			return Shapes.empty();
		}

		return Shapes.block();
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
		if (level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(entity))
			explode(level, pos);
	}

	@Override
	public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
		if (pos.equals(BlockPos.containing(explosion.center())))
			return;

		explode(level, pos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide) {
			if (!(player != null && player.isCreative() && !ConfigHandler.mineExplodesWhenInCreative) && !(level.getBlockEntity(pos) instanceof IOwnable ownable && ownable.isOwnedBy(player)))
				explode(level, pos);
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public boolean activateMine(Level level, BlockPos pos) {
		return false;
	}

	@Override
	public boolean defuseMine(Level level, BlockPos pos) {
		return false;
	}

	@Override
	public void explode(Level level, BlockPos pos) {
		if (!level.isClientSide) {
			level.destroyBlock(pos, false);
			level.explode(null, pos.getX(), pos.getY() + 0.5D, pos.getZ(), ConfigHandler.smallerMineExplosion ? 2.5F : 5.0F, ConfigHandler.shouldSpawnFire, BlockUtils.getExplosionInteraction());
		}
	}

	@Override
	public boolean dropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public boolean isActive(Level level, BlockPos pos) {
		return true;
	}

	@Override
	public boolean explodesWhenInteractedWith() {
		return false;
	}

	@Override
	public boolean isDefusable() {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}

	/**
	 * Upstream picks the disguise only when {@code IDisguisable.shouldPickBlockDisguise} holds, i.e. for players that
	 * are neither in creative mode nor the owner. That check needs the player, which this branch's 4-arg vanilla hook
	 * does not supply, so the disguise is always handed out here (matching the 1.20.1 port's behaviour).
	 */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(blockDisguisedAs);
	}

	@Override
	public Block getBlockDisguisedAs() {
		return blockDisguisedAs;
	}
}
