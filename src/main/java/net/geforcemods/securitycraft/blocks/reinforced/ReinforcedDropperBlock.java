package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.blockentities.ReinforcedDropperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The reinforced counterpart of vanilla's dropper. Just a thin subclass of {@link ReinforcedDispenserBlock},
 * matching upstream and vanilla's own {@code DropperBlock}/{@code DispenserBlock} relationship - the drop
 * behaviour (inserting into a facing container instead of a raw item toss) is inherited unchanged from
 * vanilla's {@code DropperBlock#dispenseFrom}, for the same reason {@code dispenseFrom} isn't overridden on
 * {@link ReinforcedDispenserBlock}: it already resolves the block entity generically.
 */
public class ReinforcedDropperBlock extends ReinforcedDispenserBlock {
	private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

	public ReinforcedDropperBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public DispenseItemBehavior getDispenseMethod(net.minecraft.world.level.Level level, ItemStack stack) {
		return DISPENSE_BEHAVIOUR;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReinforcedDropperBlockEntity(pos, state);
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.DROPPER;
	}
}
