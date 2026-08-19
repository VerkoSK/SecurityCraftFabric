package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedPistonMovingBlockEntity;
import net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceBlock;
import net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceGateBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;

/**
 * The reinforced counterpart of vanilla's (sticky) piston base.
 *
 * <p>Dropped versus upstream: the {@code alwaysDrop} config check in {@code canHarvestBlock} (not ported,
 * see {@code ReinforcedHopperBlock}); {@code ForgeEventFactory.onPistonMovePre}/{@code onPistonMovePost}
 * (Forge-only cancelable events with no vanilla-observable side effect on their own - dropped as if never
 * cancelled); and, in {@link #isPushable}, the reinforced-obsidian / reinforced-crying-obsidian checks,
 * since this port hasn't registered those blocks yet (only vanilla obsidian/crying obsidian/respawn
 * anchor/reinforced deepslate are checked). Upstream's {@code ValidationOwnableBlockEntity} (a marker
 * subclass of its ownable block entity used by the unported Universal Owner Changer validation flow) is
 * replaced with the plain {@link OwnableBlockEntity} the rest of this port's un-customizable owned blocks
 * use; {@code Owner#isValidated} still exists on this port's {@link net.geforcemods.securitycraft.api.Owner}
 * and is checked the same way in {@link #checkIfExtend}.
 *
 * <p>Upstream's {@code ReinforcedPistonStructureResolver} (a util-package copy of vanilla's private
 * {@code PistonStructureResolver}) is inlined here as a private nested class, since this port may only add
 * new files under {@code blocks/reinforced}, {@code blockentities} and {@code inventory}.
 */
public class ReinforcedPistonBaseBlock extends PistonBaseBlock implements IReinforcedBlock, EntityBlock {
	private final float destroyTimeForOwner;

	public ReinforcedPistonBaseBlock(boolean sticky, BlockBehaviour.Properties properties) {
		super(sticky, OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.is(state.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof OwnableBlockEntity)
			checkIfExtend(level, pos, state);
	}

	public void checkIfExtend(Level level, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		boolean hasSignal = getNeighborSignal(level, pos, direction);

		if (level.getBlockEntity(pos) instanceof OwnableBlockEntity be && !be.getOwner().isValidated())
			return;

		if (hasSignal && !state.getValue(EXTENDED)) {
			if (new PistonStructureResolver(level, pos, direction, true).resolve())
				level.blockEvent(pos, this, 0, direction.get3DDataValue());
		}
		else if (!hasSignal && state.getValue(EXTENDED)) {
			BlockPos offsetPos = pos.relative(direction, 2);
			BlockState offsetState = level.getBlockState(offsetPos);
			int i = 1;

			if (offsetState.is(SCContent.REINFORCED_MOVING_PISTON) && offsetState.getValue(FACING) == direction && level.getBlockEntity(offsetPos) instanceof ReinforcedPistonMovingBlockEntity pistonTileEntity) {
				if (pistonTileEntity.isExtending() && (pistonTileEntity.getProgress(0.0F) < 0.5F || level.getGameTime() == pistonTileEntity.getLastTicked() || ((ServerLevel) level).isHandlingTick()))
					i = 2;
			}

			level.blockEvent(pos, this, i, direction.get3DDataValue());
		}
	}

	private boolean getNeighborSignal(Level level, BlockPos pos, Direction direction) { // mirrors vanilla PistonBaseBlock#getNeighborSignal, which is private
		for (Direction dir : Direction.values()) {
			if (dir != direction && level.hasSignal(pos.relative(dir), dir))
				return true;
		}

		if (level.hasSignal(pos, Direction.DOWN))
			return true;
		else {
			BlockPos posAbove = pos.above();

			for (Direction dir : Direction.values()) {
				if (dir != Direction.DOWN && level.hasSignal(posAbove.relative(dir), dir))
					return true;
			}

			return false;
		}
	}

	@Override
	public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		Direction direction = state.getValue(FACING);
		BlockState extendedState = state.setValue(EXTENDED, true);

		if (!level.isClientSide) {
			boolean isPowered = getNeighborSignal(level, pos, direction);

			if (isPowered && (id == 1 || id == 2)) {
				level.setBlock(pos, extendedState, 2);
				return false;
			}

			if (!isPowered && id == 0)
				return false;
		}

		if (id == 0) {
			if (!this.moveBlocks(level, pos, direction, true))
				return false;

			level.setBlock(pos, extendedState, 67);
			level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.25F + 0.6F);
			level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(extendedState));
		}
		else if (id == 1 || id == 2) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof ReinforcedPistonMovingBlockEntity pistonBe)
				pistonBe.finalTick();

			BlockEntity be = level.getBlockEntity(pos);
			BlockState movingPiston = SCContent.REINFORCED_MOVING_PISTON.defaultBlockState().setValue(FACING, direction).setValue(MovingPistonBlock.TYPE, isSticky ? PistonType.STICKY : PistonType.DEFAULT);

			level.setBlock(pos, movingPiston, 20);
			level.setBlockEntity(ReinforcedMovingPistonBlock.newMovingBlockEntity(pos, movingPiston, defaultBlockState().setValue(FACING, Direction.from3DDataValue(param & 7)), be != null ? be.getUpdateTag(level.registryAccess()) : null, direction, false, true));
			level.updateNeighborsAt(pos, movingPiston.getBlock());
			movingPiston.updateNeighbourShapes(level, pos, 2);

			if (isSticky) {
				BlockPos offsetPos = pos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
				BlockState offsetState = level.getBlockState(offsetPos);
				boolean flag = false;

				if (offsetState.is(SCContent.REINFORCED_MOVING_PISTON) && level.getBlockEntity(offsetPos) instanceof ReinforcedPistonMovingBlockEntity pistonBe2 && pistonBe2.getFacing() == direction && pistonBe2.isExtending()) {
					pistonBe2.finalTick();
					flag = true;
				}

				if (!flag) {
					if (id != 1 || offsetState.isAir() || !isPushable(offsetState, level, pos, offsetPos, direction.getOpposite(), false, direction) || offsetState.getPistonPushReaction() != PushReaction.NORMAL && !offsetState.is(SCContent.REINFORCED_PISTON) && !offsetState.is(SCContent.REINFORCED_STICKY_PISTON))
						level.removeBlock(pos.relative(direction), false);
					else
						moveBlocks(level, pos, direction, false);
				}
			}
			else
				level.removeBlock(pos.relative(direction), false);

			level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.15F + 0.6F);
			level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(movingPiston));
		}

		return true;
	}

	public static boolean isPushable(BlockState state, Level level, BlockPos pistonPos, BlockPos pos, Direction facing, boolean destroyBlocks, Direction direction) {
		if (pos.getY() >= level.getMinY() && pos.getY() <= level.getMaxY() && level.getWorldBorder().isWithinBounds(pos)) {
			if (state.isAir())
				return true;
			else if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.RESPAWN_ANCHOR) && !state.is(Blocks.REINFORCED_DEEPSLATE)) {
				if ((facing == Direction.DOWN && pos.getY() == level.getMinY()) || (facing == Direction.UP && pos.getY() == level.getMaxY()))
					return false;
				else {
					boolean isPushableSCBlock = state.getBlock() instanceof IReinforcedBlock || state.getBlock() instanceof ElectrifiedIronFenceBlock || state.getBlock() instanceof ElectrifiedIronFenceGateBlock;

					if (!state.is(Blocks.PISTON) && !state.is(Blocks.STICKY_PISTON) && !state.is(SCContent.REINFORCED_PISTON) && !state.is(SCContent.REINFORCED_STICKY_PISTON)) {
						if (isPushableSCBlock) {
							if (!isSameOwner(pos, pistonPos, level))
								return false;
						}
						else if (state.getDestroySpeed(level, pos) == -1.0F)
							return false;

						switch (state.getPistonPushReaction()) {
							case BLOCK:
								return false;
							case DESTROY:
								return destroyBlocks;
							case PUSH_ONLY:
								return facing == direction;
							default:
								break;
						}
					}
					else if (state.getValue(EXTENDED))
						return false;

					return !state.hasBlockEntity() || isPushableSCBlock;
				}
			}
		}

		return false;
	}

	private boolean moveBlocks(Level level, BlockPos pos, Direction facing, boolean extending) {
		BlockPos frontPos = pos.relative(facing);
		BlockEntity pistonBe = level.getBlockEntity(pos);

		if (!extending && level.getBlockState(frontPos).is(SCContent.REINFORCED_PISTON_HEAD))
			level.setBlock(frontPos, Blocks.AIR.defaultBlockState(), 20);

		PistonStructureResolver structureResolver = new PistonStructureResolver(level, pos, facing, extending);

		if (!structureResolver.resolve())
			return false;
		else {
			Map<BlockPos, BlockState> stateToPosMap = Maps.newHashMap();
			List<BlockPos> blocksToMove = structureResolver.getToPush();
			List<BlockState> statesToMove = Lists.newArrayList();

			for (int i = 0; i < blocksToMove.size(); ++i) {
				BlockPos posToMove = blocksToMove.get(i);
				BlockState stateToMove = level.getBlockState(posToMove);

				statesToMove.add(stateToMove);
				stateToPosMap.put(posToMove, stateToMove);
			}

			List<BlockPos> blocksToDestroy = structureResolver.getToDestroy();
			BlockState[] updatedBlocks = new BlockState[blocksToMove.size() + blocksToDestroy.size()];
			Direction direction = extending ? facing : facing.getOpposite();
			int j = 0;

			for (int k = blocksToDestroy.size() - 1; k >= 0; --k) {
				BlockPos posToDestroy = blocksToDestroy.get(k);
				BlockState stateToDestroy = level.getBlockState(posToDestroy);
				BlockEntity beToDestroy = stateToDestroy.hasBlockEntity() ? level.getBlockEntity(posToDestroy) : null;

				dropResources(stateToDestroy, level, posToDestroy, beToDestroy);
				level.setBlock(posToDestroy, Blocks.AIR.defaultBlockState(), 18);
				level.gameEvent(GameEvent.BLOCK_DESTROY, posToDestroy, GameEvent.Context.of(stateToDestroy));

				if (!stateToDestroy.is(BlockTags.FIRE))
					level.addDestroyBlockEffect(posToDestroy, stateToDestroy);

				updatedBlocks[j++] = stateToDestroy;
			}

			for (int l = blocksToMove.size() - 1; l >= 0; --l) {
				BlockPos posToMove = blocksToMove.get(l);
				BlockState stateToMove = level.getBlockState(posToMove);
				BlockState movingPiston = SCContent.REINFORCED_MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
				BlockEntity beToMove = level.getBlockEntity(posToMove);
				CompoundTag tag = null;

				posToMove = posToMove.relative(direction);

				if (beToMove != null) {
					tag = beToMove.saveWithoutMetadata(level.registryAccess());
					tag.putInt("x", posToMove.getX());
					tag.putInt("y", posToMove.getY());
					tag.putInt("z", posToMove.getZ());
				}

				stateToPosMap.remove(posToMove);
				level.setBlock(posToMove, SCContent.REINFORCED_MOVING_PISTON.defaultBlockState().setValue(FACING, facing), 68);
				level.setBlockEntity(ReinforcedMovingPistonBlock.newMovingBlockEntity(posToMove, movingPiston, statesToMove.get(l), tag, facing, extending, false));
				updatedBlocks[j++] = stateToMove;
			}

			if (extending) {
				PistonType type = isSticky ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState pistonHead = SCContent.REINFORCED_PISTON_HEAD.defaultBlockState().setValue(FACING, facing).setValue(PistonHeadBlock.TYPE, type);
				BlockState movingPiston = SCContent.REINFORCED_MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, facing).setValue(MovingPistonBlock.TYPE, isSticky ? PistonType.STICKY : PistonType.DEFAULT);
				OwnableBlockEntity headBe = new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY, frontPos, movingPiston);

				if (pistonBe instanceof OwnableBlockEntity ownable) //synchronize owner to the piston head
					headBe.setOwner(ownable.getOwner().getName(), ownable.getOwner().getUUID());

				stateToPosMap.remove(frontPos);
				level.setBlock(frontPos, movingPiston, 68);
				level.setBlockEntity(ReinforcedMovingPistonBlock.newMovingBlockEntity(frontPos, movingPiston, pistonHead, headBe.getUpdateTag(level.registryAccess()), facing, true, true));
			}

			BlockState air = Blocks.AIR.defaultBlockState();

			for (BlockPos position : stateToPosMap.keySet()) {
				level.setBlock(position, air, 82);
			}

			for (Entry<BlockPos, BlockState> entry : stateToPosMap.entrySet()) {
				BlockPos posToUpdate = entry.getKey();
				BlockState stateToUpdate = entry.getValue();

				stateToUpdate.updateIndirectNeighbourShapes(level, posToUpdate, 2);
				air.updateNeighbourShapes(level, posToUpdate, 2);
				air.updateIndirectNeighbourShapes(level, posToUpdate, 2);
			}

			j = 0;

			for (int i1 = blocksToDestroy.size() - 1; i1 >= 0; --i1) {
				BlockState updatedState = updatedBlocks[j++];
				BlockPos posToDestroy = blocksToDestroy.get(i1);

				updatedState.updateIndirectNeighbourShapes(level, posToDestroy, 2);
				level.updateNeighborsAt(posToDestroy, updatedState.getBlock());
			}

			for (int j1 = blocksToMove.size() - 1; j1 >= 0; --j1) {
				level.updateNeighborsAt(blocksToMove.get(j1), updatedBlocks[j++].getBlock());
			}

			if (extending)
				level.updateNeighborsAt(frontPos, SCContent.REINFORCED_PISTON_HEAD);

			return true;
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return OwnershipUtils.newBlockEntity(pos, state);
	}

	@Override
	public Block getVanillaBlock() {
		return isSticky ? Blocks.STICKY_PISTON : Blocks.PISTON;
	}

	@Override
	public BlockState convertToVanilla(BlockState reinforcedState) {
		return IReinforcedBlock.super.convertToVanilla(reinforcedState).setValue(EXTENDED, false);
	}

	@Override
	public BlockState convertToReinforced(BlockState vanillaState) {
		return IReinforcedBlock.super.convertToReinforced(vanillaState).setValue(EXTENDED, false);
	}

	private static boolean isSameOwner(BlockPos blockPos, BlockPos pistonPos, Level level) {
		BlockEntity pistonBe = level.getBlockEntity(pistonPos);
		IOwnable blockBe = (IOwnable) level.getBlockEntity(blockPos);

		if (pistonBe instanceof IOwnable ownable)
			return blockBe.isOwnedBy(ownable.getOwner());

		return false;
	}

	/**
	 * Mirrors vanilla's package-private {@code PistonStructureResolver}, using {@link #isPushable} in place
	 * of vanilla's own pushability check so that reinforced/electrified blocks are gated on ownership.
	 */
	private static class PistonStructureResolver {
		private final Level level;
		private final BlockPos pistonPos;
		private final boolean extending;
		private final BlockPos startPos;
		private final Direction pushDirection;
		private final List<BlockPos> toPush = Lists.newArrayList();
		private final List<BlockPos> toDestroy = Lists.newArrayList();
		private final Direction pistonDirection;

		private PistonStructureResolver(Level level, BlockPos pos, Direction pistonFacing, boolean extending) {
			this.level = level;
			pistonPos = pos;
			pistonDirection = pistonFacing;
			this.extending = extending;

			if (extending) {
				pushDirection = pistonFacing;
				startPos = pos.relative(pistonFacing);
			}
			else {
				pushDirection = pistonFacing.getOpposite();
				startPos = pos.relative(pistonFacing, 2);
			}
		}

		private boolean resolve() {
			BlockState state = level.getBlockState(startPos);

			toPush.clear();
			toDestroy.clear();

			if (!isPushable(state, level, pistonPos, startPos, pushDirection, false, pistonDirection)) {
				if (extending && state.getPistonPushReaction() == PushReaction.DESTROY) {
					toDestroy.add(startPos);
					return true;
				}
				else
					return false;
			}
			else if (!addBlockLine(startPos, pushDirection))
				return false;
			else {
				for (int i = 0; i < toPush.size(); ++i) {
					BlockPos pos = toPush.get(i);

					if (isSticky(level.getBlockState(pos)) && !addBranchingBlocks(pos))
						return false;
				}

				return true;
			}
		}

		private boolean addBlockLine(BlockPos originPos, Direction facing) {
			BlockState state = level.getBlockState(originPos);

			if (level.isEmptyBlock(originPos))
				return true;
			else if (!isPushable(state, level, pistonPos, originPos, pushDirection, false, facing))
				return true;
			else if (originPos.equals(pistonPos))
				return true;
			else if (toPush.contains(originPos))
				return true;
			else {
				int i = 1;
				if (i + toPush.size() > 12)
					return false;
				else {
					BlockState oldState;

					while ((state.is(Blocks.SLIME_BLOCK) || state.is(Blocks.HONEY_BLOCK))) {
						BlockPos offsetPos = originPos.relative(pushDirection.getOpposite(), i);

						oldState = state;
						state = level.getBlockState(offsetPos);

						if (state.isAir() || !canStickToEachOther(oldState, state) || !isPushable(state, level, pistonPos, offsetPos, pushDirection, false, pushDirection.getOpposite()) || offsetPos.equals(pistonPos))
							break;

						++i;

						if (i + toPush.size() > 12)
							return false;
					}

					int l = 0;

					for (int i1 = i - 1; i1 >= 0; --i1) {
						toPush.add(originPos.relative(pushDirection.getOpposite(), i1));
						++l;
					}

					int j1 = 1;

					while (true) {
						BlockPos offsetPos = originPos.relative(pushDirection, j1);

						int j = toPush.indexOf(offsetPos);

						if (j > -1) {
							reorderListAtCollision(l, j);

							for (int k = 0; k <= j + l; ++k) {
								BlockPos posToPush = toPush.get(k);

								if (isSticky(level.getBlockState(posToPush)) && !addBranchingBlocks(posToPush))
									return false;
							}

							return true;
						}

						state = level.getBlockState(offsetPos);

						if (state.isAir())
							return true;

						if (!isPushable(state, level, pistonPos, offsetPos, pushDirection, true, pushDirection) || offsetPos.equals(pistonPos))
							return false;

						if (state.getPistonPushReaction() == PushReaction.DESTROY) {
							toDestroy.add(offsetPos);
							return true;
						}

						if (toPush.size() >= 12)
							return false;

						toPush.add(offsetPos);
						++l;
						++j1;
					}
				}
			}
		}

		private void reorderListAtCollision(int offsets, int index) {
			List<BlockPos> list = Lists.newArrayList();
			List<BlockPos> list1 = Lists.newArrayList();
			List<BlockPos> list2 = Lists.newArrayList();

			list.addAll(toPush.subList(0, index));
			list1.addAll(toPush.subList(toPush.size() - offsets, toPush.size()));
			list2.addAll(toPush.subList(index, toPush.size() - offsets));
			toPush.clear();
			toPush.addAll(list);
			toPush.addAll(list1);
			toPush.addAll(list2);
		}

		private boolean addBranchingBlocks(BlockPos fromPos) {
			BlockState state = level.getBlockState(fromPos);

			for (Direction direction : Direction.values()) {
				if (direction.getAxis() != pushDirection.getAxis()) {
					BlockPos offsetPos = fromPos.relative(direction);
					BlockState offsetState = level.getBlockState(offsetPos);

					if (canStickToEachOther(offsetState, state) && !addBlockLine(offsetPos, direction))
						return false;
				}
			}

			return true;
		}

		private List<BlockPos> getToPush() {
			return toPush;
		}

		private List<BlockPos> getToDestroy() {
			return toDestroy;
		}
	}
	/** Mirrors vanilla {@code PistonBaseBlock#isStickyBlock}, which is private. */
	private static boolean isSticky(BlockState state) {
		return state.is(Blocks.SLIME_BLOCK) || state.is(Blocks.HONEY_BLOCK);
	}

	/** Mirrors vanilla {@code PistonBaseBlock#canStickToEachOther}, which is private. */
	private static boolean canStickToEachOther(BlockState first, BlockState second) {
		if (first.is(Blocks.HONEY_BLOCK) && second.is(Blocks.SLIME_BLOCK))
			return false;
		else if (first.is(Blocks.SLIME_BLOCK) && second.is(Blocks.HONEY_BLOCK))
			return false;

		return isSticky(first) || isSticky(second);
	}

}
