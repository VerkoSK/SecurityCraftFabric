package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;
import net.minecraft.block.Block;

/** Client entrypoint: keypad screen packet + reinforced grey tint. */
public class SecurityCraftClient implements ClientModInitializer {
	private static final int REINFORCED_TINT = 0x999999;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.ID, (payload, context) -> context.client().execute(() -> context.client().setScreen(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));

		Block[] reinforced = SCContent.REINFORCED_BLOCKS.toArray(new Block[0]);
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? REINFORCED_TINT : -1, reinforced);
	}
}
