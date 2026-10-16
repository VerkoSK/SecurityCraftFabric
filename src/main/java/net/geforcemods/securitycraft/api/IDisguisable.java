package net.geforcemods.securitycraft.api;

import java.util.Optional;

import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IDisguisable extends IOverlayDisplay {
	default ItemStack getDisguisedStack(BlockGetter level, BlockPos pos) {
		if (level != null && level.getBlockEntity(pos) instanceof IModuleInventory be) {
			ItemStack stack = be.isModuleEnabled(ModuleType.DISGUISE) ? be.getModule(ModuleType.DISGUISE) : ItemStack.EMPTY;

			if (!stack.isEmpty()) {
				Block block = ModuleItem.getBlockAddon(stack);

				if (block != null)
					return new ItemStack(block);
			}
		}

		return getDefaultStack();
	}

	default ItemStack getDefaultStack() {
		return new ItemStack((Block) this);
	}

	@Override
	default ItemStack getDisplayStack(Level level, BlockState state, BlockPos pos) {
		return getDisguisedStack(level, pos);
	}

	@Override
	default boolean shouldShowSCInfo(Level level, BlockState state, BlockPos pos) {
		return getDisguisedStack(level, pos).getItem() == ((Block) this).asItem();
	}

	public static Optional<BlockState> getDisguisedBlockState(BlockGetter level, BlockPos pos) {
		if (level != null && level.getBlockEntity(pos) instanceof IModuleInventory be)
			return getDisguisedBlockState(be);

		return Optional.empty();
	}

	public static Optional<BlockState> getDisguisedBlockState(BlockEntity be, BlockGetter level) {
		if (be instanceof IModuleInventory moduleInv)
			return getDisguisedBlockState(moduleInv);

		return Optional.empty();
	}

	public static Optional<BlockState> getDisguisedBlockState(IModuleInventory be) {
		ItemStack module = be.isModuleEnabled(ModuleType.DISGUISE) ? be.getModule(ModuleType.DISGUISE) : ItemStack.EMPTY;

		if (!module.isEmpty() && be instanceof BlockEntity blockEntity)
			return Optional.ofNullable(getDisguisedBlockStateFromStack(blockEntity, module));

		return Optional.empty();
	}

	public static BlockState getDisguisedBlockStateFromStack(BlockEntity be, ItemStack module) {
		CompoundTag moduleTag = module.getOrCreateTag();
		BlockState disguisedState = NbtUtils.readBlockState(be.getLevel().holderLookup(Registries.BLOCK), moduleTag.getCompound("SavedState"));

		if (disguisedState != null && disguisedState.getBlock() != Blocks.AIR)
			return disguisedState;
		else { //fallback, mainly for upgrading old worlds from before the state selector existed
			Block block = ModuleItem.getBlockAddon(module);

			if (block != null) {
				StandingOrWallType type = StandingOrWallType.values()[moduleTag.getInt("StandingOrWall")];

				return type.getState(block);
			}
		}

		return null;
	}

	public static BlockState getDisguisedStateOrDefault(BlockState state, BlockGetter level, BlockPos pos) {
		return getDisguisedBlockState(level, pos).orElse(state);
	}

	default ItemStack getCloneItemStack(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		if (IDisguisable.shouldPickDisguise(level, pos, player))
			return getDisguisedStack(level, pos);

		return getDefaultStack();
	}

	static boolean shouldPickDisguise(BlockGetter level, BlockPos pos, Player player) {
		return !player.isCreative() && !(level.getBlockEntity(pos) instanceof IOwnable ownable && ownable.isOwnedBy(player));
	}
}
