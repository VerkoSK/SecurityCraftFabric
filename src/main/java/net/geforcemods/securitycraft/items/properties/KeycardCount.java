package net.geforcemods.securitycraft.items.properties;

import com.mojang.serialization.MapCodec;

import net.geforcemods.securitycraft.items.KeycardHolderItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record KeycardCount() implements RangeSelectItemModelProperty {
	public static final MapCodec<KeycardCount> MAP_CODEC = MapCodec.unit(new KeycardCount());

	@Override
	public float get(ItemStack stack, ClientLevel level, net.minecraft.world.entity.ItemOwner entity, int seed) {
		return KeycardHolderItem.getCardCount(stack) / (float) KeycardHolderItem.SLOTS;
	}

	@Override
	public MapCodec<? extends RangeSelectItemModelProperty> type() {
		return MAP_CODEC;
	}
}
