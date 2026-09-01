package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Renders the keypad chest (whose block model is empty) as a chest, including the double-chest join.
 *
 * <p>Upstream (and the pre-1.21.6 Fabric port) also swap in the keypad chest's own "active/inactive" artwork by
 * overriding the chest material hook. 1.21.6+ moved chest texture selection into {@code ChestRenderState} with no
 * extension point, and NeoForge's {@code customSprite} patch has no Fabric equivalent, so this port draws the
 * keypad chest with the vanilla chest texture. Recorded in {@code PORT_GAP.md}.
 */
public class KeypadChestRenderer extends ChestRenderer<ChestBlockEntity> {
	public KeypadChestRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}
}
