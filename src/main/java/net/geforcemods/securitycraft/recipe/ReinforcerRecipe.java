package net.geforcemods.securitycraft.recipe;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.BlockReinforcerItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Crafting-table recipe: combine a Universal Block Reinforcer/Remover with a matching block to convert it, damaging the tool. */
public final class ReinforcerRecipe {
	private ReinforcerRecipe() {}

	public abstract static class Base extends CustomRecipe {
		private final CraftingBookCategory category;

		protected Base(CraftingBookCategory category) {
			this.category = category;
		}

		@Override
		public CraftingBookCategory category() {
			return category;
		}

		protected abstract Block target(Block input);

		protected abstract boolean isCorrectTool(BlockReinforcerItem item);

		@Override
		public boolean matches(CraftingInput inv, Level level) {
			boolean hasBlock = false;
			boolean hasTool = false;

			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getItem(i);
				Item item = stack.getItem();

				if (item instanceof BlockItem blockItem) {
					if (hasBlock || target(blockItem.getBlock()) == null)
						return false;

					hasBlock = true;
				}
				else if (item instanceof BlockReinforcerItem reinforcer) {
					if (hasTool || !isCorrectTool(reinforcer))
						return false;

					hasTool = true;
				}
				else if (!stack.isEmpty())
					return false;
			}

			return hasBlock && hasTool;
		}

		@Override
		public ItemStack assemble(CraftingInput inv) {
			for (int i = 0; i < inv.size(); i++) {
				if (inv.getItem(i).getItem() instanceof BlockItem blockItem) {
					Block result = target(blockItem.getBlock());

					if (result != null)
						return new ItemStack(result);
				}
			}

			return ItemStack.EMPTY;
		}

		@Override
		public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
			NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

			for (int i = 0; i < inv.size(); i++) {
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
		protected boolean isCorrectTool(BlockReinforcerItem item) {
			return item.isReinforcing();
		}

		@Override
		public RecipeSerializer<? extends CustomRecipe> getSerializer() {
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
		protected boolean isCorrectTool(BlockReinforcerItem item) {
			return !item.isReinforcing();
		}

		@Override
		public RecipeSerializer<? extends CustomRecipe> getSerializer() {
			return SCContent.BLOCK_UNREINFORCING_SERIALIZER;
		}
	}
}
