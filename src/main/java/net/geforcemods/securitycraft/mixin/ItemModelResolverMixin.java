package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.renderers.KeypadChestItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Routes the keypad chest item's model resolution to {@link KeypadChestItemModel} instead of whatever its item
 * model json resolves to. fabric-api's BuiltinItemRendererRegistry, the previous hook for this (a Fabric
 * counterpart of Forge's BlockEntityWithoutLevelRenderer), was removed for this Minecraft version with no
 * replacement; this mixin is the workaround.
 */
@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
	@Inject(method = "appendItemLayers", at = @At("HEAD"), cancellable = true)
	private void securitycraft$keypadChest(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level level, LivingEntity entity, int seed, CallbackInfo ci) {
		if (stack.getItem() == SCContent.KEYPAD_CHEST.asItem()) {
			KeypadChestItemModel.INSTANCE.update(renderState, stack, (ItemModelResolver) (Object) this, displayContext, level instanceof ClientLevel clientLevel ? clientLevel : null, entity, seed);
			ci.cancel();
		}
	}
}
