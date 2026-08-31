package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Renders the keypad chest in the world. On this Minecraft version the chest render pipeline moved to extracted
 * render states with a fixed set of chest materials, so this simply reuses vanilla's {@link ChestRenderer}; the
 * keypad chest's own "active"/"inactive" artwork and the in-world disguise render are not carried over here (see
 * PORT_GAP.md).
 */
public class KeypadChestRenderer extends ChestRenderer<ChestBlockEntity> {
	public KeypadChestRenderer(BlockEntityRendererProvider.Context ctx) {
		super(ctx);
	}
}
