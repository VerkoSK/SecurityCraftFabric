package net.geforcemods.securitycraft.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Let your object implement this to be able to add modules to it.
 *
 * <p>The upstream mod additionally extends NeoForge's {@code IItemHandlerModifiable} so modules can be
 * inserted by hoppers; Fabric has no such capability interface, so it is dropped (the slot methods are
 * kept for internal use / a future Transfer-API wrapper). Automation of the module inventory is the only
 * behaviour lost by this adaptation.
 *
 * @author bl4ckscor3
 */
public interface IModuleInventory {
	public NonNullList<ItemStack> getInventory();

	public ModuleType[] acceptedModules();

	public boolean isModuleEnabled(ModuleType module);

	public void toggleModuleState(ModuleType module, boolean shouldBeEnabled);

	public Level myLevel();

	public BlockPos myPos();

	public default int getMaxNumberOfModules() {
		return acceptedModules().length;
	}

	public default void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		toggleModuleState(module, true);

		if (this instanceof BlockEntity be && !be.getLevel().isClientSide) {
			be.setChanged();
			be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
		}
	}

	public default void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		toggleModuleState(module, false);

		if (this instanceof BlockEntity be && !be.getLevel().isClientSide) {
			be.setChanged();
			be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
		}
	}

	public default boolean enableHack() {
		return false;
	}

	public default int fixSlotId(int id) {
		return id >= 100 ? id - 100 : id;
	}

	public default void dropAllModules() {
		for (ItemStack module : getInventory()) {
			if (!(module.getItem() instanceof ModuleItem))
				continue;

			if (this instanceof LinkableBlockEntity be)
				be.propagate(new ILinkedAction.ModuleRemoved(((ModuleItem) module.getItem()).getModuleType(), false), be);

			Block.popResource(myLevel(), myPos(), module);
		}

		getInventory().clear();
	}

	public default int getSlots() {
		return acceptedModules().length;
	}

	public default ItemStack getStackInSlot(int slot) {
		return getModuleInSlot(slot);
	}

	public default ItemStack getModuleInSlot(int slot) {
		slot = fixSlotId(slot);
		return slot < 0 || slot >= getSlots() ? ItemStack.EMPTY : getInventory().get(slot);
	}

	public default ItemStack extractItem(int slot, int amount, boolean simulate) {
		slot = fixSlotId(slot);

		ItemStack stack = getModuleInSlot(slot).copy();

		if (stack.isEmpty())
			return ItemStack.EMPTY;
		else {
			if (!simulate) {
				getInventory().set(slot, ItemStack.EMPTY);

				if (stack.getItem() instanceof ModuleItem module) {
					onModuleRemoved(stack, module.getModuleType(), false);

					if (this instanceof LinkableBlockEntity be)
						be.propagate(new ILinkedAction.ModuleRemoved(((ModuleItem) stack.getItem()).getModuleType(), false), be);
				}

				return stack;
			}
			else
				return stack.copy();
		}
	}

	public default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		slot = fixSlotId(slot);

		if (!getModuleInSlot(slot).isEmpty())
			return stack;
		else {
			int returnSize = 0;

			//the max stack size is one, so in order to provide the correct return value, the count after insertion is calculated here
			if (stack.getCount() > 1)
				returnSize = stack.getCount() - 1;

			if (!simulate) {
				ItemStack copy = stack.copy();

				copy.setCount(1);
				getInventory().set(slot, copy);

				if (stack.getItem() instanceof ModuleItem module) {
					onModuleInserted(stack, module.getModuleType(), false);

					if (this instanceof LinkableBlockEntity be)
						be.propagate(new ILinkedAction.ModuleInserted(copy, (ModuleItem) copy.getItem(), false), be);
				}
			}

			if (returnSize != 0) {
				ItemStack toReturn = stack.copy();

				toReturn.setCount(returnSize);
				return toReturn;
			}
			else
				return ItemStack.EMPTY;
		}
	}

	public default void setStackInSlot(int slot, ItemStack stack) {
		slot = fixSlotId(slot);

		ItemStack previous = getModuleInSlot(slot);

		//Prevent module from being removed and re-added when the slot initializes
		if (ItemStack.matches(previous, stack))
			return;

		//call the correct methods, should there have been a module in the slot previously
		if (!previous.isEmpty()) {
			onModuleRemoved(previous, ((ModuleItem) previous.getItem()).getModuleType(), false);

			if (this instanceof LinkableBlockEntity be)
				be.propagate(new ILinkedAction.ModuleRemoved(((ModuleItem) previous.getItem()).getModuleType(), false), be);
		}

		getInventory().set(slot, stack);

		if (stack.getItem() instanceof ModuleItem module) {
			onModuleInserted(stack, module.getModuleType(), false);

			if (this instanceof LinkableBlockEntity be)
				be.propagate(new ILinkedAction.ModuleInserted(stack, (ModuleItem) stack.getItem(), false), be);
		}
	}

	public default int getSlotLimit(int slot) {
		return 1;
	}

	public default boolean isItemValid(int slot, ItemStack stack) {
		slot = fixSlotId(slot);
		return getModuleInSlot(slot).isEmpty() && !stack.isEmpty() && stack.getItem() instanceof ModuleItem module && acceptsModule(module.getModuleType()) && !hasModule(module.getModuleType());
	}

	public default boolean acceptsModule(ModuleType type) {
		for (ModuleType module : acceptedModules()) {
			if (module == type)
				return true;
		}

		return false;
	}

	public default List<ModuleType> getInsertedModules() {
		ArrayList<ModuleType> modules = new ArrayList<>();

		for (ItemStack stack : getInventory()) {
			if (!stack.isEmpty() && stack.getItem() instanceof ModuleItem module)
				modules.add(module.getModuleType());
		}

		return modules;
	}

	public default ItemStack getModule(ModuleType module) {
		NonNullList<ItemStack> modules = getInventory();

		for (int i = 0; i < modules.size(); i++) {
			if (!modules.get(i).isEmpty() && modules.get(i).getItem() instanceof ModuleItem moduleItem && moduleItem.getModuleType() == module)
				return modules.get(i);
		}

		return ItemStack.EMPTY;
	}

	public default void insertModule(ItemStack module, boolean toggled) {
		if (module.isEmpty() || !(module.getItem() instanceof ModuleItem moduleItem))
			return;

		NonNullList<ItemStack> modules = getInventory();

		//if the module is being toggled, then there should not be a check for whether the module already exists
		if (!toggled) {
			for (int i = 0; i < modules.size(); i++) {
				if (!modules.get(i).isEmpty() && modules.get(i).getItem() == moduleItem)
					return;
			}
		}

		//if the module is being toggled, the test should be for the stack that matches the module. if not, the test should look for the first empty slot
		Predicate<ItemStack> predicate = toggled ? stack -> stack.getItem() == moduleItem : ItemStack::isEmpty;

		for (int i = 0; i < modules.size(); i++) {
			if (predicate.test(modules.get(i))) {
				ItemStack toInsert = module.copy();

				if (!toggled)
					modules.set(i, toInsert);

				onModuleInserted(toInsert, moduleItem.getModuleType(), toggled);
				break;
			}
		}
	}

	public default void removeModule(ModuleType module, boolean toggled) {
		NonNullList<ItemStack> modules = getInventory();

		for (int i = 0; i < modules.size(); i++) {
			ItemStack moduleStack = modules.get(i);

			if (!moduleStack.isEmpty() && moduleStack.getItem() instanceof ModuleItem moduleItem && moduleItem.getModuleType() == module) {
				if (!toggled)
					modules.set(i, ItemStack.EMPTY);

				onModuleRemoved(moduleStack, module, toggled);
			}
		}
	}

	@Deprecated
	public default boolean hasModule(ModuleType module) {
		NonNullList<ItemStack> modules = getInventory();

		if (module == null) {
			for (int i = 0; i < modules.size(); i++) {
				if (modules.get(i).isEmpty())
					return true;
			}
		}
		else {
			for (int i = 0; i < modules.size(); i++) {
				if (!modules.get(i).isEmpty() && modules.get(i).getItem() instanceof ModuleItem moduleItem && moduleItem.getModuleType() == module)
					return true;
			}
		}

		return false;
	}

	public default NonNullList<ItemStack> readModuleInventory(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		ListTag list = tag.getList("Modules", Tag.TAG_COMPOUND);
		NonNullList<ItemStack> modules = NonNullList.withSize(getMaxNumberOfModules(), ItemStack.EMPTY);

		for (int i = 0; i < list.size(); ++i) {
			CompoundTag stackTag = list.getCompound(i);
			byte slot = stackTag.getByte("ModuleSlot");

			if (slot >= 0 && slot < modules.size())
				modules.set(slot, Utils.parseOptional(lookupProvider, stackTag));
		}

		return modules;
	}

	public default Map<ModuleType, Boolean> readModuleStates(CompoundTag tag) {
		EnumMap<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);
		List<ModuleType> acceptedModules = Arrays.asList(acceptedModules());

		for (ModuleType module : ModuleType.values()) {
			if (acceptedModules.contains(module)) {
				String key = module.name().toLowerCase() + "Enabled";

				if (tag.contains(key))
					moduleStates.put(module, tag.getBoolean(key));
				else
					moduleStates.put(module, hasModule(module)); //if the module is accepted, but no state was saved yet, revert to whether the module is installed
			}
			else
				moduleStates.put(module, false); //module is not accepted, so disable it right away
		}

		return moduleStates;
	}

	public default CompoundTag writeModuleInventory(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		ListTag list = new ListTag();
		NonNullList<ItemStack> modules = getInventory();

		for (int i = 0; i < modules.size(); i++) {
			if (!modules.get(i).isEmpty()) {
				CompoundTag stackTag = new CompoundTag();

				stackTag.putByte("ModuleSlot", (byte) i);
				list.add(modules.get(i).save(lookupProvider, stackTag));
			}
		}

		tag.put("Modules", list);
		return tag;
	}

	public default CompoundTag writeModuleStates(CompoundTag tag) {
		for (ModuleType module : acceptedModules()) {
			tag.putBoolean(module.name().toLowerCase() + "Enabled", isModuleEnabled(module));
		}

		return tag;
	}

	public default boolean isAllowed(Entity entity) {
		String name;

		if (this instanceof IOwnable ownable && entity instanceof Player player && ownable.isOwnedBy(player, true))
			name = player.getName().getString();
		else
			name = entity.getName().getString();

		return isAllowed(name);
	}

	public default boolean isAllowed(String name) {
		if (!isModuleEnabled(ModuleType.ALLOWLIST))
			return false;

		ListModuleData listModuleData = getModule(ModuleType.ALLOWLIST).get(SCContent.LIST_MODULE_DATA);

		return listModuleData != null && (listModuleData.affectEveryone() || listModuleData.isTeamOfPlayerOnList(myLevel(), name) || listModuleData.isPlayerOnList(name));
	}

	public default boolean isDenied(Entity entity) {
		if (!isModuleEnabled(ModuleType.DENYLIST))
			return false;

		ListModuleData listModuleData = getModule(ModuleType.DENYLIST).get(SCContent.LIST_MODULE_DATA);
		String name;

		if (listModuleData != null) {
			if (listModuleData.affectEveryone()) {
				if (this instanceof IOwnable ownable) {
					//only deny players that are not the owner
					if (entity instanceof Player player) {
						//if the player IS the owner, fall back to the default handling (check if the name is on the list)
						if (!ownable.isOwnedBy(player))
							return true;
					}
					else
						return true;
				}
				else
					return true;
			}

			if (this instanceof IOwnable ownable && entity instanceof Player player && ownable.isOwnedBy(player, true))
				name = player.getName().getString();
			else
				name = entity.getName().getString();

			return listModuleData.isTeamOfPlayerOnList(myLevel(), name) || listModuleData.isPlayerOnList(name);
		}

		return false;
	}

	public default boolean shouldDropModules() {
		return true;
	}

	public default String getModuleDescriptionId(String denotation, ModuleType module) {
		if (module == ModuleType.DISGUISE)
			return "module.generic.disguise_module.description";

		return getBaseModuleDescriptionId(denotation, module);
	}

	public static String getBaseModuleDescriptionId(String denotation, ModuleType module) {
		return "module." + denotation + "." + module.getTranslationKey().substring(5).replace("securitycraft.", "") + ".description";
	}
}
