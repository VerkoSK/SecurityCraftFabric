package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

/** Client entrypoint. Wires the keypad screen packet and translucent render layer for glass blocks. */
public class SecurityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> context.client().setScreen(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));

		for (Block glass : SCContent.GLASS_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(glass, RenderType.translucent());
	}
}
