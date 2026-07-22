package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.BlockReinforcerItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Crafting-table recipe: combine a Universal Block Reinforcer/Remover with a matching block to convert it, damaging the tool. */
public final class ReinforcerRecipe {
	private ReinforcerRecipe() {}

	public abstract static class Base extends CustomRecipe {
		protected Base(CraftingBookCategory category) {
			super(category);
		}

		protected abstract Block target(Block input);

		protected abstract boolean isCorrectTool(BlockReinforcerItem item, ItemStack stack);

		@Override
		public boolean matches(CraftingContainer inv, Level level) {
			boolean hasBlock = false;
			boolean hasTool = false;

			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack stack = inv.getItem(i);
				Item item = stack.getItem();

				if (item instanceof BlockItem blockItem) {
					if (hasBlock || target(blockItem.getBlock()) == null)
						return false;

					hasBlock = true;
				}
				else if (item instanceof BlockReinforcerItem reinforcer) {
					if (hasTool || !isCorrectTool(reinforcer, stack))
						return false;

					hasTool = true;
				}
				else if (!stack.isEmpty())
					return false;
			}

			return hasBlock && hasTool;
		}

		@Override
		public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider provider) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				if (inv.getItem(i).getItem() instanceof BlockItem blockItem) {
					Block result = target(blockItem.getBlock());

					if (result != null)
						return new ItemStack(result);
				}
			}

			return ItemStack.EMPTY;
		}

		@Override
		public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
			NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack stack = inv.getItem(i);

				if (stack.getItem() instanceof BlockReinforcerItem) {
					ItemStack copy = stack.copy();

					if (copy.getMaxDamage() > 0) {
						int dmg = copy.getDamageValue() + 1;

						if (dmg >= copy.getMaxDamage())
							copy = ItemStack.EMPTY;
						else
							copy.setDamageValue(dmg);
					}

					remaining.set(i, copy);
				}
			}

			return remaining;
		}

		@Override
		public boolean canCraftInDimensions(int width, int height) {
			return width * height >= 2;
		}
	}

	public static class Reinforcing extends Base {
		public Reinforcing(CraftingBookCategory category) {
			super(category);
		}

		@Override
		protected Block target(Block input) {
			return SCContent.reinforcedCounterpart(input);
		}

		@Override
		protected boolean isCorrectTool(BlockReinforcerItem item, ItemStack stack) {
			return item.isReinforcing(stack);
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return SCContent.BLOCK_REINFORCING_SERIALIZER;
		}
	}

	public static class Unreinforcing extends Base {
		public Unreinforcing(CraftingBookCategory category) {
			super(category);
		}

		@Override
		protected Block target(Block input) {
			return SCContent.vanillaCounterpart(input);
		}

		@Override
		protected boolean isCorrectTool(BlockReinforcerItem item, ItemStack stack) {
			return !item.isReinforcing(stack);
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return SCContent.BLOCK_UNREINFORCING_SERIALIZER;
		}
	}
}
