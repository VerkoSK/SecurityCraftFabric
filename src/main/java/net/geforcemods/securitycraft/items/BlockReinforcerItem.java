package net.geforcemods.securitycraft.items;

import java.util.Map;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Converts blocks between vanilla and reinforced. Lvl1 always reinforces, the remover always
 * unreinforces, and Lvl2/Lvl3 default to reinforcing but can be toggled to unreinforcing (stored as a
 * boolean NBT flag, since MC 1.20.1 has no data components). Damages the tool per use when it has durability.
 */
public class BlockReinforcerItem extends Item {
	private static final String UNREINFORCING_KEY = "unreinforcing";
	/** The item's base capability: true = a reinforcer (Lvl1/2/3), false = the remover. */
	private final boolean reinforcing;

	public BlockReinforcerItem(Item.Properties properties, boolean reinforcing) {
		super(properties);
		this.reinforcing = reinforcing;
	}

	/** Base capability (ignores the toggle). Used by the crafting-table recipes. */
	public boolean isReinforcing() {
		return reinforcing;
	}

	/** Effective mode of a specific stack, honouring the Lvl2/Lvl3 toggle. */
	public boolean isReinforcing(ItemStack stack) {
		if (!reinforcing)
			return false;

		if (stack.is(SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL1))
			return true;

		return !(stack.hasTag() && stack.getTag().getBoolean(UNREINFORCING_KEY));
	}

	/** Whether the mode of this stack may be toggled (Lvl2/Lvl3 only). */
	public boolean canToggleMode(ItemStack stack) {
		return reinforcing && !stack.is(SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL1);
	}

	public static void setReinforcing(ItemStack stack, boolean reinforcing) {
		if (reinforcing) {
			if (stack.hasTag())
				stack.getTag().remove(UNREINFORCING_KEY);
		}
		else
			stack.getOrCreateTag().putBoolean(UNREINFORCING_KEY, true);
	}

	@Override
	public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		if (level instanceof net.minecraft.server.level.ServerLevel) {
			maybeRemoveMending(held);
			player.openMenu(new net.minecraft.world.SimpleMenuProvider((windowId, inv, p) -> new net.geforcemods.securitycraft.inventory.BlockReinforcerMenu(windowId, inv), held.getHoverName()));
		}

		return net.minecraft.world.InteractionResultHolder.consume(held);
	}

	/** Strips any Mending enchantment so the consumable reinforcer cannot self-repair (1:1 with upstream). */
	public static void maybeRemoveMending(ItemStack stack) {
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

		if (enchantments.containsKey(Enchantments.MENDING)) {
			enchantments.remove(Enchantments.MENDING);
			EnchantmentHelper.setEnchantments(enchantments, stack);
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		ItemStack stack = ctx.getItemInHand();
		net.minecraft.world.entity.player.Player player = ctx.getPlayer();
		Block target = isReinforcing(stack) ? SCContent.reinforcedCounterpart(state.getBlock()) : SCContent.vanillaCounterpart(state.getBlock());

		if (target == null || player == null || player.isCreative() || !level.mayInteract(player, pos))
			return InteractionResult.PASS;

		if (level instanceof ServerLevel) {
			level.setBlockAndUpdate(pos, target.withPropertiesOf(state));
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
		}

		return InteractionResult.SUCCESS;
	}
}
