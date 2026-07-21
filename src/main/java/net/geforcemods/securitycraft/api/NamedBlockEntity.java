package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NamedBlockEntity extends OwnableBlockEntity implements Nameable {
	private Component customName;

	public NamedBlockEntity(BlockPos pos, BlockState state) {
		this(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}

	public NamedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable("CustomName", ComponentSerialization.CODEC, customName);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		customName = parseCustomNameSafe(input, "CustomName");

		if (customName == null) { //Conversion of old string literal names
			String legacyName = input.getString("customName").orElse(null);

			if (legacyName != null)
				customName = Component.literal(legacyName);
		}
	}

	@Override
	public Component getName() {
		return hasCustomName() ? getCustomName() : getDefaultName();
	}

	@Override
	public boolean hasCustomName() {
		Component name = getCustomName();

		return name != null && !Component.empty().equals(name) && !getDefaultName().equals(name);
	}

	@Override
	public Component getCustomName() {
		return customName;
	}

	public void setCustomName(Component customName) {
		this.customName = customName;
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public Component getDefaultName() {
		return Utils.localize(getBlockState().getBlock().getDescriptionId());
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter input) {
		super.applyImplicitComponents(input);
		customName = input.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.CUSTOM_NAME, customName);
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		output.discard("CustomName");
		output.discard("customName");
	}
}
