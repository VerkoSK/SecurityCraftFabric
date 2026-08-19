package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the keypad chest item in hand and in the inventory, since its block model is empty. Upstream does this
 * with Forge's {@code BlockEntityWithoutLevelRenderer}; Fabric's counterpart is this one-method interface.
 */
public class KeypadChestItemRenderer implements DynamicItemRenderer {
	private KeypadChestBlockEntity dummyBe;
	private KeypadChestRenderer dummyRenderer;

	@Override
	public void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (dummyRenderer == null) {
			Minecraft mc = Minecraft.getInstance();

			dummyRenderer = new KeypadChestRenderer(new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font));
		}

		if (dummyBe == null)
			dummyBe = new KeypadChestBlockEntity(BlockPos.ZERO, SCContent.KEYPAD_CHEST.defaultBlockState());

		dummyRenderer.render(dummyBe, 0.0F, pose, buffer, combinedLight, combinedOverlay);
	}
}
