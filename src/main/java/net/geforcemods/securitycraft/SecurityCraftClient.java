package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.geforcemods.securitycraft.misc.TintMode;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.KeypadScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;

/** Client entrypoint: keypad screen packet + configurable reinforced-block tint (see {@link TintMode}). */
public class SecurityCraftClient implements ClientModInitializer {
	/** Live tint colour for reinforced blocks: white base multiplied by the configured tint (grey by default). */
	private static int reinforcedTint() {
		return TintMode.tint(Minecraft.getInstance().player, 0xFFFFFFFF, null);
	}

	@Override
	public void onInitializeClient() {
		SCClientConfig.load();
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> context.client().setScreen(new KeypadScreen(payload.pos(), payload.setup(), payload.ownerName()))));
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.BLOCK_REINFORCER_MENU, net.geforcemods.securitycraft.screen.BlockReinforcerScreen::new);

		Block[] reinforced = SCContent.REINFORCED_BLOCKS.toArray(new Block[0]);
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? reinforcedTint() : -1, reinforced);

		// Glass panes use a flat (item/generated) icon; the grey tint on a translucent texture makes it near-invisible, so skip it for their items.
		java.util.List<Block> itemTinted = new java.util.ArrayList<>(SCContent.REINFORCED_BLOCKS);
		itemTinted.removeAll(SCContent.GLASS_PANE_BLOCKS);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? reinforcedTint() : -1, itemTinted.toArray(new Block[0]));

		for (Block glass : SCContent.GLASS_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(glass, RenderType.translucent());

		for (Block cutout : SCContent.CUTOUT_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());

		// Laser beam: translucent animated texture, tinted red at tintindex 0 (the lens colour).
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.LASER_FIELD, RenderType.translucent());
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? 0xFF0000 : -1, SCContent.LASER_FIELD);
	}
}
