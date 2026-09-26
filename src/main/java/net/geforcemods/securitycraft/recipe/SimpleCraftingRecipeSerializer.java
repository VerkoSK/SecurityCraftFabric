package net.geforcemods.securitycraft.recipe;

import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleCraftingRecipeSerializer {
	public static <T extends CraftingRecipe> RecipeSerializer<T> create(Function<CraftingBookCategory, T> factory) {
		return new RecipeSerializer<>(MapCodec.unit(() -> factory.apply(CraftingBookCategory.MISC)), StreamCodec.unit(factory.apply(CraftingBookCategory.MISC)));
	}

	public static <T extends CraftingRecipe> RecipeSerializer<T> create(Supplier<T> factory) {
		return new RecipeSerializer<>(MapCodec.unit(factory::get), StreamCodec.unit(factory.get()));
	}
}
