package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ReinforcedIronBarsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ReinforcedIronBarsBlock extends ReinforcedPaneBlock {
	public ReinforcedIronBarsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, net.minecraft.world.level.LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
		FluidState fluidState = level.getFluidState(pos);
		BlockPos northPos = pos.north();
		BlockPos southPos = pos.south();
		BlockPos westPos = pos.west();
		BlockPos eastPos = pos.east();
		BlockState northState = level.getBlockState(northPos);
		BlockState southState = level.getBlockState(southPos);
		BlockState westState = level.getBlockState(westPos);
		BlockState eastState = level.getBlockState(eastPos);
		return defaultBlockState().setValue(NORTH, attachsTo(northState, northState.isFaceSturdy(level, northPos, Direction.SOUTH))).setValue(SOUTH, attachsTo(southState, southState.isFaceSturdy(level, southPos, Direction.NORTH))).setValue(WEST, attachsTo(westState, westState.isFaceSturdy(level, westPos, Direction.EAST))).setValue(EAST, attachsTo(eastState, eastState.isFaceSturdy(level, eastPos, Direction.WEST))).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be, ItemStack stack) {
		if (be instanceof ReinforcedIronBarsBlockEntity ironBarsBe && !ironBarsBe.canDrop()) {
			player.causeFoodExhaustion(0.005F);
			return;
		}

		super.playerDestroy(level, player, pos, state, be, stack);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedIronBarsBlockEntity(pos, state);
	}
}
