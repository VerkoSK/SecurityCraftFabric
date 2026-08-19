package net.geforcemods.securitycraft.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;

/** 1:1 with the upstream record of the same name, minus the boat branch (this port has no security sea boats). */
public record SCManualPage(Item item, PageGroup group, Component title, Component helpInfo, String designedBy, boolean hasRecipeDescription) {
	public Object getInWorldObject() {
		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();

			if (block.defaultBlockState().hasBlockEntity())
				return ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
		}

		return null;
	}
}
