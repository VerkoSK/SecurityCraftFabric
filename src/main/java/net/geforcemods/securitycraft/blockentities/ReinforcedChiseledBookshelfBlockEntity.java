package net.geforcemods.securitycraft.blockentities;

import java.util.EnumMap;
import java.util.Map;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The reinforced counterpart of vanilla's chiseled bookshelf block entity: owner + allowlist module layered
 * on top of vanilla's item-storing logic, which is otherwise untouched.
 *
 * <p>Dropped versus upstream: the Forge {@code IItemHandler} capability (insert-only handler for
 * non-owners) - per the task's Fabric notes, this relies on the vanilla {@code Container} interface that
 * {@code ChiseledBookShelfBlockEntity} already implements instead.
 */
public class ReinforcedChiseledBookshelfBlockEntity extends ChiseledBookShelfBlockEntity implements IOwnable, IModuleInventory {
	private NonNullList<ItemStack> modules = NonNullList.withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private Owner owner = new Owner();
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);

	public ReinforcedChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public BlockEntityType<?> getType() {
		return SCContent.REINFORCED_CHISELED_BOOKSHELF_BLOCK_ENTITY;
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		owner = Owner.load(input);
		modules = readModuleInventory(input);
		moduleStates = readModuleStates(input);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		if (owner != null)
			owner.save(output, needsValidation());

		writeModuleInventory(output);
		writeModuleStates(output);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
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
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST
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
