package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NamedBlockEntity extends OwnableBlockEntity implements Nameable {
	private Component customName;

	public NamedBlockEntity(BlockPos pos, BlockState state) {
		this(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}

	public NamedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		if (customName != null)
			tag.putString("CustomName", Component.Serializer.toJson(customName));
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("CustomName"))
			customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		else if (tag.contains("customName")) //Conversion of old string literal names
			customName = Component.literal(tag.getString("customName"));
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
}
