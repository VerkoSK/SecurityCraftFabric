package net.geforcemods.securitycraft.api;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class LinkableBlockEntity extends CustomizableBlockEntity implements ITickingBlockEntity {
	private List<LinkedBlock> pendingLinkedBlocks = null;

	protected LinkableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (hasLevel() && pendingLinkedBlocks != null) {
			linkLoadedBlocks(pendingLinkedBlocks);
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
			pendingLinkedBlocks = null;
		}
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		List<LinkedBlock> savedLinkedBlocks = parseLinkedBlocks(input);

		if (!savedLinkedBlocks.isEmpty()) {
			if (!hasLevel())
				pendingLinkedBlocks = savedLinkedBlocks;
			else
				linkLoadedBlocks(savedLinkedBlocks);
		}
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		saveLinkedBlocks(output);
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		propagate(new ILinkedAction.OptionChanged<>(option), this);
		super.onOptionChanged(option);
	}

	/** Parses the raw linked-block data (name + position) without touching the level. */
	private static List<LinkedBlock> parseLinkedBlocks(ValueInput input) {
		List<LinkedBlock> parsed = new ArrayList<>();

		for (ValueInput entry : input.childrenListOrEmpty("linkedBlocks")) {
			String name = entry.getStringOr("blockName", "");
			int x = entry.getIntOr("blockX", 0);
			int y = entry.getIntOr("blockY", 0);
			int z = entry.getIntOr("blockZ", 0);

			parsed.add(new LinkedBlock(name, new BlockPos(x, y, z)));
		}

		return parsed;
	}

	/** Establishes the actual links for already-parsed blocks; requires the level to be present. */
	private void linkLoadedBlocks(List<LinkedBlock> blocks) {
		ImmutableList<LinkedBlock> linkedBlocks = getLinkedBlocks();

		for (LinkedBlock block : blocks) {
			if (hasLevel() && level.isLoaded(block.getPos()) && block.validate(level) && !linkedBlocks.contains(block))
				link(this, block.asBlockEntity(level));
		}
	}

	/**
	 * Links two blocks together. Calls onLinkedBlockAction() whenever certain events (found in {@link ILinkedAction}) occur.
	 */
	public static void link(LinkableBlockEntity blockEntity1, LinkableBlockEntity blockEntity2) {
		if (isLinkedWith(blockEntity1, blockEntity2))
			return;

		LinkedBlock block1 = new LinkedBlock(blockEntity1);
		LinkedBlock block2 = new LinkedBlock(blockEntity2);

		if (!blockEntity1.getLinkedBlocks().contains(block2)) {
			blockEntity1.addLinkedBlock(block2);
			blockEntity1.setChanged();
		}

		if (!blockEntity2.getLinkedBlocks().contains(block1)) {
			blockEntity2.addLinkedBlock(block1);
			blockEntity2.setChanged();
		}
	}

	/**
	 * Unlinks the second block entity from the first.
	 */
	public static void unlink(LinkableBlockEntity blockEntity1, LinkableBlockEntity blockEntity2) {
		if (blockEntity1 == null || blockEntity2 == null)
			return;

		LinkedBlock block = new LinkedBlock(blockEntity2);

		if (blockEntity1.getLinkedBlocks().contains(block)) {
			blockEntity1.removeLinkedBlock(block);
			blockEntity1.setChanged();
		}
	}

	/**
	 * Unlinks the block entity from all blocks that it is linked to.
	 */
	public static void unlinkFromAllLinked(LinkableBlockEntity blockEntity) {
		if (blockEntity == null)
			return;

		Level level = blockEntity.level;

		for (LinkedBlock block : blockEntity.getLinkedBlocks()) {
			if (level.isLoaded(block.getPos()))
				LinkableBlockEntity.unlink(block.asBlockEntity(level), blockEntity);
		}

		blockEntity.setChanged();
	}

	/**
	 * @return Are the two blocks linked together?
	 */
	public static boolean isLinkedWith(LinkableBlockEntity blockEntity1, LinkableBlockEntity blockEntity2) {
		return blockEntity1.getLinkedBlocks().contains(new LinkedBlock(blockEntity2)) && blockEntity2.getLinkedBlocks().contains(new LinkedBlock(blockEntity1));
	}

	/**
	 * Calls onLinkedBlockAction() for every block this block entity is linked to.
	 */
	public void propagate(ILinkedAction action, LinkableBlockEntity excludedBE) {
		ArrayList<LinkableBlockEntity> list = new ArrayList<>();

		list.add(excludedBE);
		propagate(action, list);
	}

	/**
	 * Calls onLinkedBlockAction() for every valid block this block entity is linked to, and removes invalid blocks from the
	 * linked block list.
	 */
	public void propagate(ILinkedAction action, List<LinkableBlockEntity> excludedBEs) {
		List<LinkedBlock> blocksToRemove = new ArrayList<>();

		for (LinkedBlock block : getLinkedBlocks()) {
			if (level.isLoaded(block.getPos()) && !excludedBEs.contains(block.asBlockEntity(level))) {
				if (block.validate(level)) {
					BlockState state = level.getBlockState(block.getPos());

					block.asBlockEntity(level).onLinkedBlockAction(action, excludedBEs);
					level.sendBlockUpdated(block.getPos(), state, state, 3);
				}
				else
					blocksToRemove.add(block);
			}
		}

		for (LinkedBlock blockToRemove : blocksToRemove) {
			removeLinkedBlock(blockToRemove);
		}
	}

	protected abstract ImmutableList<LinkedBlock> getLinkedBlocks();

	protected abstract void addLinkedBlock(LinkedBlock block);

	protected abstract void removeLinkedBlock(LinkedBlock block);

	protected void onLinkedBlockAction(ILinkedAction action, List<LinkableBlockEntity> excludedBEs) {}

	protected void saveLinkedBlocks(ValueOutput output) {
		List<LinkedBlock> linkedBlocks = getLinkedBlocks();

		if (!linkedBlocks.isEmpty()) {
			ValueOutput.ValueOutputList list = output.childrenList("linkedBlocks");

			for (LinkedBlock block : linkedBlocks) {
				if (block != null) {
					ValueOutput entry = list.addChild();

					entry.putString("blockName", block.getBlockName());
					entry.putInt("blockX", block.getX());
					entry.putInt("blockY", block.getY());
					entry.putInt("blockZ", block.getZ());
				}
			}
		}
	}
}
