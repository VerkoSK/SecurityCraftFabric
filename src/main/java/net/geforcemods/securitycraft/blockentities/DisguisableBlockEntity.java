package net.geforcemods.securitycraft.blockentities;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockEntityRenderDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DisguisableBlockEntity extends CustomizableBlockEntity implements RenderDataBlockEntity {
	public static final BlockEntityRenderDelegate DISGUISED_BLOCK_RENDER_DELEGATE = new BlockEntityRenderDelegate();

	protected DisguisableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		if (level != null && module == ModuleType.DISGUISE) {
			if (level.isClientSide) {
				BlockState disguised = IDisguisable.getDisguisedBlockStateFromStack(stack).orElse(null);
				DISGUISED_BLOCK_RENDER_DELEGATE.putDelegateFor(this, disguised);
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
			if (level.isClientSide) {
				DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);
				refreshModel();
			}
			else
				sync();
		}
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.loadAdditional(tag, lookupProvider);

		if (level != null && level.isClientSide) {
			if (isModuleEnabled(ModuleType.DISGUISE)) {
				BlockState disguised = IDisguisable.getDisguisedBlockStateFromStack(getModule(ModuleType.DISGUISE)).orElse(null);
				DISGUISED_BLOCK_RENDER_DELEGATE.putDelegateFor(this, disguised);
			}
			else
				DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);

			refreshModel();
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		if (level != null && level.isClientSide)
			DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);
	}

	@Override
	public Object getRenderData() {
		return IDisguisable.getDisguisedBlockState(this).orElse(null);
	}

	private void refreshModel() {
		if (level != null && level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
	}
}
