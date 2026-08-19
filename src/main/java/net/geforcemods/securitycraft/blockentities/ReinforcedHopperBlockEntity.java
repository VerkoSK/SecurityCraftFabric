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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The reinforced counterpart of vanilla's hopper block entity: owner + allowlist/disguise modules layered
 * on top of vanilla's item-moving logic, which is otherwise untouched.
 *
 * <p>Vanilla's {@code HopperBlockEntity} only has a (Mojang-mapped) public constructor that hardcodes
 * vanilla's own {@code BlockEntityType} - there's no protected overload taking one, unlike
 * {@code DispenserBlockEntity}. Upstream (via a Forge access transformer) works around this the same way
 * this port has to on plain Mojang mappings: construct through the vanilla constructor and override
 * {@link #getType()} to report the real type; nothing else reads the field the vanilla constructor set.
 *
 * <p>Dropped versus upstream: the Forge {@code IItemHandler} capability (insert-only handler for
 * non-owners, extraction locking) - the task's Fabric notes say to rely on vanilla's {@code Container}
 * instead, and this port has no Transfer API wrapper yet, so hoppers/pipes reach this like any vanilla
 * hopper; and the module-inventory {@code getStackInSlot}/{@code getItem} slot-100 aliasing, which existed
 * only to expose module slots through that same capability.
 */
public class ReinforcedHopperBlockEntity extends HopperBlockEntity implements IOwnable, IModuleInventory, net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity {
	private NonNullList<ItemStack> modules = NonNullList.withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private Owner owner = new Owner();
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);

	public ReinforcedHopperBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public BlockEntityType<?> getType() {
		return SCContent.REINFORCED_HOPPER_BLOCK_ENTITY;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		owner.load(tag);
		modules = readModuleInventory(tag);
		moduleStates = readModuleStates(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		owner.save(tag, needsValidation());
		writeModuleInventory(tag);
		writeModuleStates(tag);
	}

	@Override
	protected Component getDefaultName() {
		//no dedicated SCContent field for this block (it's registered data-driven, by name), so read the
		//description id off the block itself, the way NamedBlockEntity#getDefaultName does
		return Utils.localize(getBlockState().getBlock().getDescriptionId());
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

	@Override
	public boolean needsValidation() {
		return true;
	}

	/** The block state this hopper is disguised as (from an enabled disguise module), or null. */
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
