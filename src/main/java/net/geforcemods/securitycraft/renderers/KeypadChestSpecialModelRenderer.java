package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the keypad chest item in hand and in the inventory, since its block model is empty. Wired in through
 * {@link net.geforcemods.securitycraft.mixin.ItemModelResolverMixin}, since fabric-api's BuiltinItemRendererRegistry
 * (this port's previous Forge-BlockEntityWithoutLevelRenderer equivalent) was removed for this Minecraft version
 * with no replacement, leaving vanilla's own SpecialModelRenderer as the only extension point left.
 */
public class KeypadChestSpecialModelRenderer implements SpecialModelRenderer<Void> {
	public static final KeypadChestSpecialModelRenderer INSTANCE = new KeypadChestSpecialModelRenderer();
	private KeypadChestBlockEntity dummyBe;
	private KeypadChestRenderer dummyRenderer;

	private KeypadChestSpecialModelRenderer() {}

	@Override
	public void render(Void unused, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay, boolean hasFoil) {
		if (dummyRenderer == null) {
			Minecraft mc = Minecraft.getInstance();

			dummyRenderer = new KeypadChestRenderer(new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font));
		}

		if (dummyBe == null)
			dummyBe = new KeypadChestBlockEntity(BlockPos.ZERO, SCContent.KEYPAD_CHEST.defaultBlockState());

		dummyRenderer.render(dummyBe, 0.0F, pose, buffer, combinedLight, combinedOverlay);
	}

	@Override
	public Void extractArgument(ItemStack stack) {
		return null;
	}
}
