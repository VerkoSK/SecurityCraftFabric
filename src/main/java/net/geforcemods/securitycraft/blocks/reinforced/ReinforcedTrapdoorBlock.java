package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

/** Reinforced iron trapdoor: opens only when a powered SecurityCraft keypad is adjacent — NOT by regular redstone. */
public class ReinforcedTrapdoorBlock extends TrapdoorBlock {
	public ReinforcedTrapdoorBlock(BlockSetType type, AbstractBlock.Settings settings) {
		super(type, settings);
	}

	private static boolean hasActiveKeypadNextTo(World world, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			BlockState neighbor = world.getBlockState(pos.offset(dir));

			if (neighbor.getBlock() instanceof KeypadBlock && neighbor.get(Properties.POWERED))
				return true;
		}

		return false;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState base = super.getPlacementState(ctx);

		if (base == null)
			return null;

		boolean active = hasActiveKeypadNextTo(ctx.getWorld(), ctx.getBlockPos());
		return base.with(Properties.OPEN, active).with(Properties.POWERED, active);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify) {
		boolean active = hasActiveKeypadNextTo(world, pos);

		if (active != state.get(Properties.OPEN))
			world.setBlockState(pos, state.with(Properties.OPEN, active).with(Properties.POWERED, active), 2);
	}
}
