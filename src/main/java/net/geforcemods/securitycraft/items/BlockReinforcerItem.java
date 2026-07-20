package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Converts blocks between vanilla and reinforced. Damages the tool per use when it has durability. */
public class BlockReinforcerItem extends Item {
	private final boolean reinforcing;

	public BlockReinforcerItem(Item.Settings settings, boolean reinforcing) {
		super(settings);
		this.reinforcing = reinforcing;
	}

	public boolean isReinforcing() {
		return reinforcing;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		BlockState state = world.getBlockState(pos);
		Block target = reinforcing ? SCContent.reinforcedCounterpart(state.getBlock()) : SCContent.vanillaCounterpart(state.getBlock());

		if (target == null)
			return ActionResult.PASS;

		if (world instanceof ServerWorld) {
			world.setBlockState(pos, target.getStateWithProperties(state));

			ItemStack stack = ctx.getStack();

			if (stack.getMaxDamage() > 0) {
				int dmg = stack.getDamage() + 1;

				if (dmg >= stack.getMaxDamage())
					stack.decrement(1);
				else
					stack.setDamage(dmg);
			}
		}

		return ActionResult.SUCCESS;
	}
}
