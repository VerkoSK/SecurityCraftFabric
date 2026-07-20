package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;

/** Client entrypoint: keypad screen packet + reinforced grey tint. */
public class SecurityCraftClient implements ClientModInitializer {
	// ARGB: full alpha required in 26.x (tint sources multiply as ARGB; a 0-alpha value renders transparent).
	private static final int REINFORCED_TINT = 0xFF999999;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> net.minecraft.client.Minecraft.getInstance().setScreenAndShow(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.BLOCK_REINFORCER_MENU, net.geforcemods.securitycraft.screen.BlockReinforcerScreen::new);

		Block[] reinforced = SCContent.REINFORCED_BLOCKS.toArray(new Block[0]);
		// tintIndex 0 -> grey. List position maps to the model face's tintindex; only index 0 gets a source.
		java.util.List<BlockTintSource> tintSources = java.util.List.of(state -> REINFORCED_TINT);
		BlockColorRegistry.register(tintSources, reinforced);
		// Item icon tint and glass/cutout render layers are model-JSON concerns in 26.x
		// (items/*.json `tints`, block model `render_type`) — no runtime registry needed.
	}
}
