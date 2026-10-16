package net.geforcemods.securitycraft.util;

import java.util.function.Function;

import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum StandingOrWallType {
	NONE(Block::defaultBlockState),
	STANDING(Block::defaultBlockState),
	WALL(block -> ((StandingAndWallBlockItem) block.asItem()).wallBlock.defaultBlockState());

	private final Function<Block, BlockState> stateGetter;

	StandingOrWallType(Function<Block, BlockState> stateGetter) {
		this.stateGetter = stateGetter;
	}

	public BlockState getState(Block block) {
		return stateGetter.apply(block);
	}
}
