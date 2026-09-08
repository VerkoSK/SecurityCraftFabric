package net.geforcemods.securitycraft.items;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluid;

/**
 * The bucket for one of the two fake liquids. 1:1 with upstream's item, minus its Forge fluid-capability hook,
 * which has no Fabric counterpart (the Fabric equivalent would be a Transfer API storage; nothing in this port
 * needs one yet).
 */
public class FakeLiquidBucketItem extends BucketItem {
	public FakeLiquidBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
		super(supplier.get(), builder);

		DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				DispensibleContainerItem bucket = (DispensibleContainerItem) stack.getItem();
				BlockPos dispenseAt = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				Level level = source.level();

				if (bucket.emptyContents(null, level, dispenseAt, null)) {
					bucket.checkExtraContent(null, level, stack, dispenseAt);
					return new ItemStack(Items.BUCKET);
				}

				return defaultDispenseItemBehavior.dispense(source, stack);
			}
		});
	}
}
