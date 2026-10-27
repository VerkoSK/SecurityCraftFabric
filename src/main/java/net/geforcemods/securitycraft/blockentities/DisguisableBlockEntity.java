package net.geforcemods.securitycraft.blockentities;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DisguisableBlockEntity extends CustomizableBlockEntity implements RenderDataBlockEntity {
	protected DisguisableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		if (level != null && module == ModuleType.DISGUISE) {
			if (level.isClientSide()) {
				refreshModel();
			}
			else
				sync();
		}
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);

		if (level != null && module == ModuleType.DISGUISE) {
			if (level.isClientSide()) {
				refreshModel();
			}
			else
				sync();
		}
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		if (level != null && level.isClientSide()) {
			refreshModel();
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
	}

	@Override
	public Object getRenderData() {
		return IDisguisable.getDisguisedBlockState(this).orElse(null);
	}

	private void refreshModel() {
		if (level != null && level.isClientSide())
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
	}
}
