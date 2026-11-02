package net.geforcemods.securitycraft.recipe;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.GlobalPositionComponent;
import net.geforcemods.securitycraft.components.GlobalPositions;
import net.geforcemods.securitycraft.components.NamedPositions;
import net.geforcemods.securitycraft.items.CameraMonitorItem;
import net.geforcemods.securitycraft.items.MineRemoteAccessToolItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

@SuppressWarnings("rawtypes")
public class CopyPositionComponentItemRecipe<T extends GlobalPositionComponent> extends CombineRecipe {
	private final Supplier<Item> item;
	private final Supplier<DataComponentType<T>> component;
	private final Function<List, T> creator;
	private final T defaultInstance;
	private final Supplier<RecipeSerializer<? extends CustomRecipe>> serializer;

	public CopyPositionComponentItemRecipe(CraftingBookCategory craftingBookCategory, Supplier<Item> item, Supplier<DataComponentType<T>> component, Function<List, T> creator, T defaultInstance, Supplier<RecipeSerializer<? extends CustomRecipe>> serializer) {
		super(craftingBookCategory);
		this.item = item;
		this.component = component;
		this.creator = creator;
		this.defaultInstance = defaultInstance;
		this.serializer = serializer;
	}

	public static CopyPositionComponentItemRecipe<NamedPositions> cameraMonitor(CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe<>(craftingBookCategory, () -> SCContent.CAMERA_MONITOR, () -> SCContent.BOUND_CAMERAS, NamedPositions::new, CameraMonitorItem.DEFAULT_NAMED_POSITIONS, () -> SCContent.COPY_CAMERA_MONITOR_RECIPE_SERIALIZER);
	}

	public static CopyPositionComponentItemRecipe<GlobalPositions> mineRemoteAccessTool(CraftingBookCategory craftingBookCategory) {
		return new CopyPositionComponentItemRecipe<>(craftingBookCategory, () -> SCContent.MINE_REMOTE_ACCESS_TOOL, () -> SCContent.BOUND_MINES, GlobalPositions::new, MineRemoteAccessToolItem.DEFAULT_POSITIONS, () -> SCContent.COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER);
	}

	@Override
	public boolean matchesFirstItem(ItemStack stack) {
		return stack.is(item.get()) && !stack.getOrDefault(component.get(), defaultInstance).isEmpty();
	}

	@Override
	public boolean matchesSecondItem(ItemStack stack) {
		return stack.is(item.get()) && stack.getOrDefault(component.get(), defaultInstance).isEmpty();
	}

	@Override
	public ItemStack combine(ItemStack itemWithPositions, ItemStack emptyItem) {
		T positionsToCopy = itemWithPositions.getOrDefault(component.get(), defaultInstance);
		ItemStack result = new ItemStack(item.get(), 2);

		result.set(component.get(), creator.apply(positionsToCopy.positions()));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return serializer.get();
	}
}
