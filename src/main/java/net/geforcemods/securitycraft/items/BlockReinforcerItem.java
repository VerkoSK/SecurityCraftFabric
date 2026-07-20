package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Converts blocks between vanilla and reinforced. reinforcing=true: vanilla->reinforced; false: the reverse. Damages the tool per use when it has durability. */
public class BlockReinforcerItem extends Item {
	private final boolean reinforcing;

	public BlockReinforcerItem(Item.Properties properties, boolean reinforcing) {
		super(properties);
		this.reinforcing = reinforcing;
	}

	public boolean isReinforcing() {
		return reinforcing;
	}

	@Override
	public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		if (!level.isClientSide)
			player.openMenu(new net.minecraft.world.SimpleMenuProvider((windowId, inv, p) -> new net.geforcemods.securitycraft.inventory.BlockReinforcerMenu(windowId, inv), held.getHoverName()));

		return net.minecraft.world.InteractionResultHolder.sidedSuccess(held, level.isClientSide);
	}
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Block target = reinforcing ? SCContent.reinforcedCounterpart(state.getBlock()) : SCContent.vanillaCounterpart(state.getBlock());

		if (target == null)
			return InteractionResult.PASS;

		if (level instanceof ServerLevel) {
			level.setBlockAndUpdate(pos, target.withPropertiesOf(state));

			ItemStack stack = ctx.getItemInHand();

			if (stack.getMaxDamage() > 0) {
				int dmg = stack.getDamageValue() + 1;

				if (dmg >= stack.getMaxDamage())
					stack.shrink(1);
				else
					stack.setDamageValue(dmg);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
