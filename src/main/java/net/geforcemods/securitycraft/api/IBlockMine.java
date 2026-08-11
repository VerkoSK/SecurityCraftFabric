package net.geforcemods.securitycraft.api;

import net.minecraft.world.level.block.Block;

/**
 * Block mines that expose the block they are disguised as. 1:1 with the upstream {@code api.IBlockMine}. Not to be
 * confused with the {@link net.geforcemods.securitycraft.util.IBlockMine} marker interface, which is what
 * {@link net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock} implements.
 */
public interface IBlockMine {
	public Block getBlockDisguisedAs();
}
