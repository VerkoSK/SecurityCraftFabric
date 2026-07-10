package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;

/**
 * Client entrypoint. Wires the keypad screen packet. Reinforced-glass translucency is declared
 * natively in its block model ({@code "render_type": "minecraft:translucent"}) since Fabric's
 * BlockRenderLayerMap was removed once Mojang added model render types.
 */
public class SecurityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> context.client().setScreen(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));
	}
}
