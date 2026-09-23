package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Reinforces a vanilla block into its reinforced counterpart in place, or - in remove mode - instantly destroys a
 * reinforced block and drops it, bypassing its normal (much slower) break time, matching upstream's
 * UniversalBlockRemoverItem. Lvl1 always reinforces, the remover always removes, and Lvl2/Lvl3 default to
 * reinforcing but can be toggled to remove mode (stored in the {@link SCContent#UNREINFORCING} data component). Damages the tool per use when it has durability.
 */
public class BlockReinforcerItem extends Item {
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

		return !stack.has(SCContent.UNREINFORCING);
	}

	/** Whether the mode of this stack may be toggled (Lvl2/Lvl3 only). */
	public boolean canToggleMode(ItemStack stack) {
		return reinforcing && !stack.is(SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL1);
	}

	public static void setReinforcing(ItemStack stack, boolean reinforcing) {
		if (reinforcing)
			stack.remove(SCContent.UNREINFORCING);
		else
			stack.set(SCContent.UNREINFORCING, Unit.INSTANCE);
	}

	@Override
	public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		if (level instanceof net.minecraft.server.level.ServerLevel) {
			maybeRemoveMending(level.registryAccess(), held);
			player.openMenu(new net.minecraft.world.SimpleMenuProvider((windowId, inv, p) -> new net.geforcemods.securitycraft.inventory.BlockReinforcerMenu(windowId, inv), held.getHoverName()));
		}

		return InteractionResult.CONSUME;
	}

	/** Strips any Mending enchantment so the consumable reinforcer cannot self-repair (1:1 with upstream). */
	public static void maybeRemoveMending(net.minecraft.core.HolderLookup.Provider lookupProvider, ItemStack stack) {
		net.minecraft.world.item.enchantment.ItemEnchantments enchantments = stack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
		net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> mending = lookupProvider.lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).get().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING);

		if (enchantments != null && enchantments.getLevel(mending) > 0) {
			net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(enchantments);

			mutable.set(mending, 0);
			stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, mutable.toImmutable());
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
		//thus bypass) somebody else's blocks. Silent like that gate too: upstream doesn't message here either
		if (!isReinforcing(stack) && level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.api.IOwnable ownable) {
			net.geforcemods.securitycraft.api.Owner owner = ownable.getOwner();

			if (owner.owns() && !ownable.isOwnedBy(player) && !net.geforcemods.securitycraft.ConfigHandler.allowBreakingNonOwnedBlocks)
				return InteractionResult.FAIL;
		}

		if (level instanceof ServerLevel) {
			if (isReinforcing(stack)) {
				net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
				net.minecraft.nbt.CompoundTag tag = null;

				if (be != null) {
					tag = be.saveWithoutMetadata(level.registryAccess());

					if (be instanceof net.minecraft.world.Clearable clearable)
						clearable.clearContent();
				}

				level.setBlockAndUpdate(pos, target.withPropertiesOf(state));

				if (tag != null) {
					BlockEntity newBe = BlockEntity.loadStatic(pos, level.getBlockState(pos), tag, level.registryAccess());

					if (newBe != null)
						level.setBlockEntity(newBe);
				}

				net.geforcemods.securitycraft.util.OwnershipUtils.setPlacedBy(level, pos, player);

				if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
					DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
					BlockPos otherHalfPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
					BlockState otherHalfState = level.getBlockState(otherHalfPos);

					if (otherHalfState.is(state.getBlock())) {
						net.minecraft.world.level.block.entity.BlockEntity otherBe = level.getBlockEntity(otherHalfPos);
						net.minecraft.nbt.CompoundTag otherTag = null;

						if (otherBe != null) {
							otherTag = otherBe.saveWithoutMetadata(level.registryAccess());

							if (otherBe instanceof net.minecraft.world.Clearable clearable)
								clearable.clearContent();
						}

						level.setBlockAndUpdate(otherHalfPos, target.withPropertiesOf(otherHalfState));

						if (otherTag != null) {
							BlockEntity newOtherBe = BlockEntity.loadStatic(otherHalfPos, level.getBlockState(otherHalfPos), otherTag, level.registryAccess());

							if (newOtherBe != null)
								level.setBlockEntity(newOtherBe);
						}

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
				stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(ctx.getHand()));
		}

		return InteractionResult.SUCCESS;
	}
}
