package net.geforcemods.securitycraft.blocks.reinforced;


import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedCauldronBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The reinforced counterpart of vanilla's water/powder-snow cauldrons - both are instances of this class,
 * constructed with different fill predicates, interaction maps and vanilla-block references, exactly like
 * upstream. Dropped versus upstream: the {@code alwaysDrop} config check in {@code canHarvestBlock} (not
 * ported, see {@code ReinforcedHopperBlock}).
 */
public class ReinforcedLayeredCauldronBlock extends LayeredCauldronBlock implements IReinforcedBlock, EntityBlock {
	private final Block vanillaBlock;
	private final float destroyTimeForOwner;

	public ReinforcedLayeredCauldronBlock(BlockBehaviour.Properties properties, Precipitation precipitation, CauldronInteraction.Dispatcher interactions, Block vanillaBlock) {
		super(precipitation, interactions, OwnableBlock.withReinforcedDestroyTime(properties));
		this.vanillaBlock = vanillaBlock;
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
		if (collisionContext instanceof EntityCollisionContext ctx && ctx.getEntity() != null) {
			Entity entity = ctx.getEntity();

			if (entity instanceof Player player) {
				if (level.getBlockEntity(pos) instanceof ReinforcedCauldronBlockEntity be && be.isAllowedToInteract(player))
					return SHAPE;
				else
					return Shapes.block();
			}
		}

		return SHAPE;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ReinforcedCauldronBlockEntity be && be.isAllowedToInteract(player))
			return super.useWithoutItem(state, level, pos, player, hit);

		return InteractionResult.PASS;
	}

	public static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
		int fillLevel = state.getValue(LEVEL) - 1;
		BlockState newState = fillLevel == 0 ? SCContent.REINFORCED_CAULDRON.defaultBlockState() : state.setValue(LEVEL, fillLevel);

		ReinforcedCauldronBlock.updateBlockState(level, pos, newState);
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean stillInside) {
		//vanilla's LayeredCauldronBlock folds the "burning entity falls into a water cauldron" handling into entityInside; mirror it, but drop the reinforced cauldron's fill level rather than vanilla's
		if (!level.isClientSide() && this == SCContent.REINFORCED_WATER_CAULDRON && entity.isOnFire() && isEntityInsideContent(state, pos, entity)) {
			entity.clearFire();
			lowerFillLevel(state, level, pos);
		}
	}

	private boolean isEntityInsideContent(BlockState state, BlockPos pos, Entity entity) {
		return entity.getY() < pos.getY() + getContentHeight(state) && entity.getBoundingBox().maxY > pos.getY() + 0.25;
	}

	@Override
	public Block getVanillaBlock() {
		return vanillaBlock;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedCauldronBlockEntity(pos, state);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(SCContent.REINFORCED_CAULDRON);
	}
}
