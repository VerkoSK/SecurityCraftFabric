package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;

/** Client entrypoint: keypad screen packet + reinforced grey tint. */
public class SecurityCraftClient implements ClientModInitializer {
	private static final int REINFORCED_TINT = 0x999999;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> context.client().setScreen(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));

		Block[] reinforced = SCContent.REINFORCED_BLOCKS.toArray(new Block[0]);
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? REINFORCED_TINT : -1, reinforced);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? REINFORCED_TINT : -1, reinforced);

		for (Block glass : SCContent.GLASS_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(glass, RenderType.translucent());

		for (Block cutout : SCContent.CUTOUT_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());
	}
}
