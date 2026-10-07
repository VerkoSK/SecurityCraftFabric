package net.geforcemods.securitycraft.items;

import java.util.Map;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Reinforces a vanilla block into its reinforced counterpart in place, or - in remove mode - instantly destroys a
 * reinforced block and drops it, bypassing its normal (much slower) break time, matching upstream's
 * UniversalBlockRemoverItem. Lvl1 always reinforces, the remover always removes, and Lvl2/Lvl3 default to
 * reinforcing but can be toggled to remove mode (stored as a boolean NBT flag, since MC 1.20.1 has no data
 * components). Damages the tool per use when it has durability.
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

		if (target == null || player == null || !level.mayInteract(player, pos))
			return InteractionResult.PASS;

		//removing reinforcement strips the block's protection entirely, so this must respect ownership just like
		//breaking the block would (OwnershipUtils#getDestroyProgress) - otherwise anyone could un-reinforce (and
		//thus bypass) somebody else's blocks.
		if (!isReinforcing(stack)) {
			BlockPos checkPos = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
			BlockEntity be = level.getBlockEntity(checkPos);

			if (be instanceof IOwnable ownable) {
				if (!ConfigHandler.allowBreakingNonOwnedBlocks && !ownable.isOwnedBy(player)) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(stack.getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", PlayerUtils.getOwnerComponent(ownable.getOwner())), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}
			}
			else if (!ConfigHandler.allowBreakingNonOwnedBlocks) {
				return InteractionResult.FAIL;
			}
		}

		if (level instanceof ServerLevel) {
			if (isReinforcing(stack)) {
				net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
				net.minecraft.nbt.CompoundTag tag = null;

				if (be != null) {
					tag = be.saveWithoutMetadata();

					if (be instanceof net.minecraft.world.Clearable clearable)
						clearable.clearContent();
				}

				level.setBlockAndUpdate(pos, target.withPropertiesOf(state));

				if (tag != null && level.getBlockEntity(pos) != null)
					level.getBlockEntity(pos).load(tag);

				net.geforcemods.securitycraft.util.OwnershipUtils.setPlacedBy(level, pos, player);

				if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
					DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
					BlockPos otherHalfPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
					BlockState otherHalfState = level.getBlockState(otherHalfPos);

					if (otherHalfState.is(state.getBlock())) {
						net.minecraft.world.level.block.entity.BlockEntity otherBe = level.getBlockEntity(otherHalfPos);
						net.minecraft.nbt.CompoundTag otherTag = null;

						if (otherBe != null) {
							otherTag = otherBe.saveWithoutMetadata();

							if (otherBe instanceof net.minecraft.world.Clearable clearable)
								clearable.clearContent();
						}

						level.setBlockAndUpdate(otherHalfPos, target.withPropertiesOf(otherHalfState));

						if (otherTag != null && level.getBlockEntity(otherHalfPos) != null)
							level.getBlockEntity(otherHalfPos).load(otherTag);

						net.geforcemods.securitycraft.util.OwnershipUtils.setPlacedBy(level, otherHalfPos, player);
					}
				}
			}
			else {
				//matches upstream's remover: it doesn't downgrade the block to its vanilla counterpart in place,
				//it instantly removes the reinforced block and drops it (bypassing the normal, much slower break
				//time), same as the original UniversalBlockRemoverItem#onItemUseFirst
				BlockPos destroyPos = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

				level.destroyBlock(destroyPos, true);
			}

			if (!player.isCreative())
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
		}

		return InteractionResult.SUCCESS;
	}
}
