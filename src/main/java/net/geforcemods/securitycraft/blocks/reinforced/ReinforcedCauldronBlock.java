package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Map;
import java.util.function.Predicate;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedCauldronBlockEntity;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The reinforced counterpart of vanilla's empty cauldron. Dropped versus upstream: the {@code alwaysDrop}
 * config check in {@code canHarvestBlock} (not ported, see {@code ReinforcedHopperBlock}).
 */
public class ReinforcedCauldronBlock extends AbstractCauldronBlock implements IReinforcedBlock, EntityBlock {
	public static final com.mojang.serialization.MapCodec<ReinforcedCauldronBlock> CODEC = simpleCodec(properties -> new ReinforcedCauldronBlock(properties, new CauldronInteraction.Dispatcher()));
	private final float destroyTimeForOwner;

	@Override
	protected com.mojang.serialization.MapCodec<? extends AbstractCauldronBlock> codec() {
		return CODEC;
	}

	public ReinforcedCauldronBlock(BlockBehaviour.Properties properties, CauldronInteraction.Dispatcher interactions) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), interactions);
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

	@Override
	public boolean isFull(BlockState state) {
		return false;
	}

	protected static boolean shouldHandlePrecipitation(Level level, Precipitation precipitation) {
		return switch (precipitation) {
			case RAIN -> level.getRandom().nextFloat() < 0.05F;
			case SNOW -> level.getRandom().nextFloat() < 0.1F;
			default -> false;
		};
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Precipitation precipitation) {
		if (shouldHandlePrecipitation(level, precipitation)) {
			BlockState newCauldronState = null;

			if (precipitation == Precipitation.RAIN)
				newCauldronState = SCContent.REINFORCED_WATER_CAULDRON.defaultBlockState();
			else if (precipitation == Precipitation.SNOW)
				newCauldronState = SCContent.REINFORCED_POWDER_SNOW_CAULDRON.defaultBlockState();

			if (newCauldronState != null) {
				updateBlockState(level, pos, newCauldronState);
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
			}
		}
	}

	@Override
	protected boolean canReceiveStalactiteDrip(Fluid fluid) {
		return true;
	}

	@Override
	protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
		BlockState newCauldronState = null;
		int levelEvent = 0;

		if (fluid == Fluids.WATER) {
			newCauldronState = SCContent.REINFORCED_WATER_CAULDRON.defaultBlockState();
			levelEvent = LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON;
		}
		else if (fluid == Fluids.LAVA) {
			newCauldronState = SCContent.REINFORCED_LAVA_CAULDRON.defaultBlockState();
			levelEvent = LevelEvent.SOUND_DRIP_LAVA_INTO_CAULDRON;
		}

		if (newCauldronState != null) {
			updateBlockState(level, pos, newCauldronState);
			level.levelEvent(levelEvent, pos, 0);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newCauldronState));
		}
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.CAULDRON;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedCauldronBlockEntity(pos, state);
	}

	public static void updateBlockState(Level level, BlockPos pos, BlockState newState) {
		updateBlockState(level, pos, newState, level.getBlockEntity(pos));
	}

	public static void updateBlockState(Level level, BlockPos pos, BlockState newState, BlockEntity be) {
		CompoundTag tag = null;

		if (be != null)
			tag = net.geforcemods.securitycraft.util.BlockUtils.saveBlockEntity(be, level);

		level.setBlockAndUpdate(pos, newState);

		if (tag != null && level.getBlockEntity(pos) != null)
			net.geforcemods.securitycraft.util.BlockUtils.loadBlockEntity(level.getBlockEntity(pos), tag, level);
	}

	/**
	 * Vanilla's {@code CauldronInteraction} maps ({@code CauldronInteraction.WATER} etc.) point every recipe at
	 * the vanilla cauldron blocks, so reinforced cauldrons need their own maps built the same way upstream
	 * builds them (without Forge's cauldron-interaction helpers, which don't exist on Fabric).
	 */
	public interface IReinforcedCauldronInteraction extends CauldronInteraction {
		static boolean isWaterPotion(ItemStack stack) {
			PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

			return contents != null && contents.is(Potions.WATER);
		}

		CauldronInteraction.Dispatcher EMPTY = new CauldronInteraction.Dispatcher();
		CauldronInteraction.Dispatcher WATER = new CauldronInteraction.Dispatcher();
		CauldronInteraction.Dispatcher LAVA = new CauldronInteraction.Dispatcher();
		CauldronInteraction.Dispatcher POWDER_SNOW = new CauldronInteraction.Dispatcher();
		CauldronInteraction FILL_WATER = (state, level, pos, player, hand, stack) -> emptyBucket(level, pos, player, hand, stack, SCContent.REINFORCED_WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
		CauldronInteraction FILL_LAVA = (state, level, pos, player, hand, stack) -> emptyBucket(level, pos, player, hand, stack, SCContent.REINFORCED_LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
		CauldronInteraction FILL_POWDER_SNOW = (state, level, pos, player, hand, stack) -> emptyBucket(level, pos, player, hand, stack, SCContent.REINFORCED_POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
		CauldronInteraction SHULKER_BOX = (state, level, pos, player, hand, stack) -> {
			Block block = Block.byItem(stack.getItem());

			if (!(block instanceof ShulkerBoxBlock))
				return InteractionResult.PASS;
			else {
				if (!level.isClientSide()) {
					player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(stack, player, stack.transmuteCopy(Blocks.SHULKER_BOX, 1), false));
					player.awardStat(Stats.CLEAN_SHULKER_BOX);
					ReinforcedLayeredCauldronBlock.lowerFillLevel(state, level, pos);
				}

				return InteractionResult.SUCCESS;
			}
		};
		CauldronInteraction BANNER = (state, level, pos, player, hand, stack) -> {
			BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);

			if (layers.layers().isEmpty())
				return InteractionResult.PASS;
			else {
				if (!level.isClientSide()) {
					ItemStack bannerCopy = stack.copyWithCount(1);

					bannerCopy.set(DataComponents.BANNER_PATTERNS, layers.removeLast());
					player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(stack, player, bannerCopy, false));
					player.awardStat(Stats.CLEAN_BANNER);
					ReinforcedLayeredCauldronBlock.lowerFillLevel(state, level, pos);
				}

				return InteractionResult.SUCCESS;
			}
		};
		CauldronInteraction DYED_ITEM = (state, level, pos, player, hand, stack) -> {
			if (!stack.has(DataComponents.DYED_COLOR))
				return InteractionResult.PASS;
			else {
				if (!level.isClientSide()) {
					stack.remove(DataComponents.DYED_COLOR);
					player.awardStat(Stats.CLEAN_ARMOR);
					ReinforcedLayeredCauldronBlock.lowerFillLevel(state, level, pos);
				}

				return InteractionResult.SUCCESS;
			}
		};

		static void bootStrap() {
			addDefaultInteractions(EMPTY);
			EMPTY.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
				if (!isWaterPotion(stack))
					return InteractionResult.PASS;
				else {
					if (!level.isClientSide()) {
						Item item = stack.getItem();

						player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
						player.awardStat(Stats.USE_CAULDRON);
						player.awardStat(Stats.ITEM_USED.get(item));
						updateBlockState(level, pos, SCContent.REINFORCED_WATER_CAULDRON.defaultBlockState());
						level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
					}

					return InteractionResult.SUCCESS;
				}
			});
			addDefaultInteractions(WATER);
			WATER.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> fillBucket(state, level, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET), s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
			WATER.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
				if (!level.isClientSide()) {
					Item item = stack.getItem();

					player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(item));
					ReinforcedLayeredCauldronBlock.lowerFillLevel(state, level, pos);
					level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
				}

				return InteractionResult.SUCCESS;
			});
			WATER.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
				if (state.getValue(LayeredCauldronBlock.LEVEL) != 3 && isWaterPotion(stack)) {
					if (!level.isClientSide()) {
						player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
						player.awardStat(Stats.USE_CAULDRON);
						player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
						updateBlockState(level, pos, state.cycle(LayeredCauldronBlock.LEVEL), null);
						level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
					}

					return InteractionResult.SUCCESS;
				}
				else
					return InteractionResult.PASS;
			});
			WATER.put(Items.LEATHER_BOOTS, DYED_ITEM);
			WATER.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
			WATER.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
			WATER.put(Items.LEATHER_HELMET, DYED_ITEM);
			WATER.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
			WATER.put(Items.BANNER.pick(DyeColor.WHITE), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.GRAY), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.BLACK), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.BLUE), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.BROWN), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.CYAN), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.GREEN), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.LIGHT_BLUE), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.LIGHT_GRAY), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.LIME), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.MAGENTA), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.ORANGE), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.PINK), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.PURPLE), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.RED), BANNER);
			WATER.put(Items.BANNER.pick(DyeColor.YELLOW), BANNER);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.WHITE), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.GRAY), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.BLACK), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.BLUE), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.BROWN), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.CYAN), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.GREEN), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.LIME), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.ORANGE), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.PINK), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.PURPLE), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.RED), SHULKER_BOX);
			WATER.put(Items.DYED_SHULKER_BOX.pick(DyeColor.YELLOW), SHULKER_BOX);
			LAVA.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> fillBucket(state, level, pos, player, hand, stack, new ItemStack(Items.LAVA_BUCKET), s -> true, SoundEvents.BUCKET_FILL_LAVA));
			addDefaultInteractions(LAVA);
			POWDER_SNOW.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> fillBucket(state, level, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET), l -> l.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
			addDefaultInteractions(POWDER_SNOW);

			//add dyeable item interactions; upstream also registers SCContent.BRIEFCASE here, which this port hasn't registered yet
			CauldronInteractions.WATER.put(SCContent.LENS, DYED_ITEM);
			WATER.put(SCContent.LENS, DYED_ITEM);
		}

		static void addDefaultInteractions(CauldronInteraction.Dispatcher interactions) {
			interactions.put(Items.LAVA_BUCKET, FILL_LAVA);
			interactions.put(Items.WATER_BUCKET, FILL_WATER);
			interactions.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
		}

		static InteractionResult fillBucket(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, ItemStack bucket, Predicate<BlockState> fillPredicate, SoundEvent sound) {
			if (!fillPredicate.test(state))
				return InteractionResult.PASS;
			else {
				if (!level.isClientSide()) {
					Item item = stack.getItem();

					player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, bucket));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(item));
					updateBlockState(level, pos, SCContent.REINFORCED_CAULDRON.defaultBlockState());
					level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
				}

				return InteractionResult.SUCCESS;
			}
		}

		static InteractionResult emptyBucket(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, BlockState state, SoundEvent sound) {
			if (!level.isClientSide()) {
				Item item = stack.getItem();

				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
				player.awardStat(Stats.FILL_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(item));
				updateBlockState(level, pos, state);
				level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}

			return InteractionResult.SUCCESS;
		}
	}
}
