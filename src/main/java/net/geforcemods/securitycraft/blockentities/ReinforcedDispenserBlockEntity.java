package net.geforcemods.securitycraft.blockentities;

import java.util.EnumMap;
import java.util.Map;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The reinforced counterpart of vanilla's dispenser block entity: owner + allowlist/disguise modules layered
 * on top of vanilla's dispensing logic, which is otherwise untouched.
 *
 * <p>Dropped versus upstream: the Forge {@code IItemHandler} capability and the module-inventory
 * {@code getStackInSlot}/{@code getItem} slot-100 aliasing that only existed to feed it - see
 * {@link ReinforcedHopperBlockEntity} for the same note.
 */
public class ReinforcedDispenserBlockEntity extends DispenserBlockEntity implements IOwnable, IModuleInventory, net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity {
	private NonNullList<ItemStack> modules = NonNullList.withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private Owner owner = new Owner();
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);

	public ReinforcedDispenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ReinforcedDispenserBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.REINFORCED_DISPENSER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		//no dedicated SCContent field for this block (it's registered data-driven, by name), so read the
		//description id off the block itself, the way NamedBlockEntity#getDefaultName does
		return Utils.localize(getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		owner.load(tag);
		modules = readModuleInventory(tag, registries);
		moduleStates = readModuleStates(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		owner.save(tag, needsValidation());
		writeModuleInventory(tag, registries);
		writeModuleStates(tag);
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String name, String uuid) {
		owner.set(name, uuid);
		setChanged();
	}

	/** The block state this dispenser is disguised as (from an enabled disguise module), or null. */
	public BlockState getDisguisedState() {
		if (isModuleEnabled(ModuleType.DISGUISE)) {
			Block addon = ModuleItem.getBlockAddon(getModule(ModuleType.DISGUISE));

			if (addon != null && addon != getBlockState().getBlock())
				return addon.defaultBlockState();
		}

		return null;
	}

	@Override
	public Object getRenderData() {
		return getDisguisedState();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DISGUISE
		};
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public boolean isModuleEnabled(ModuleType module) {
		return hasModule(module) && moduleStates.get(module) == Boolean.TRUE; //prevent NPE
	}

	@Override
	public void toggleModuleState(ModuleType module, boolean shouldBeEnabled) {
		moduleStates.put(module, shouldBeEnabled);
	}

	@Override
	public Level myLevel() {
		return level;
	}

	@Override
	public BlockPos myPos() {
		return worldPosition;
	}
}
