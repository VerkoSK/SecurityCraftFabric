package net.geforcemods.securitycraft.blockentities;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DisguisableBlockEntity extends CustomizableBlockEntity implements RenderDataBlockEntity {
	public DisguisableBlockEntity(BlockPos pos, BlockState state) {
		this(SCContent.DISGUISABLE_BLOCK_ENTITY, pos, state);
	}

	public DisguisableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		if (level != null && module == ModuleType.DISGUISE) {
			if (level.isClientSide) {
				net.geforcemods.securitycraft.SecurityCraftClient.putDisguisedRenderDelegate(this, IDisguisable.getDisguisedBlockStateFromStack(this, stack));
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
				net.geforcemods.securitycraft.SecurityCraftClient.removeDisguisedRenderDelegate(this);
				refreshModel();
			}
			else
				sync();
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (level != null && level.isClientSide) {
			if (isModuleEnabled(ModuleType.DISGUISE))
				net.geforcemods.securitycraft.SecurityCraftClient.putDisguisedRenderDelegate(this, IDisguisable.getDisguisedBlockStateFromStack(this, getModule(ModuleType.DISGUISE)));
			else
				net.geforcemods.securitycraft.SecurityCraftClient.removeDisguisedRenderDelegate(this);

			refreshModel();
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		if (level != null && level.isClientSide)
			net.geforcemods.securitycraft.SecurityCraftClient.removeDisguisedRenderDelegate(this);
	}

	@Override
	public Object getRenderData() {
		return IDisguisable.getDisguisedBlockState(this).orElse(null);
	}

	private void refreshModel() {
		if (level != null && level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DISGUISE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[0];
	}
}
