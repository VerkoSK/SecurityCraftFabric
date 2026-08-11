package net.geforcemods.securitycraft.renderers;

import org.joml.Quaternionf;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Vec3i;

/** Render state for {@link ClaymoreRenderer}: whether the claymore is deactivated, and the tripwire geometry. */
public class ClaymoreRenderState extends BlockEntityRenderState {
	public boolean isDeactivated;
	public Quaternionf rotation;
	public Vec3i normal;
	public int r;
	public int g;
	public int b;
}
