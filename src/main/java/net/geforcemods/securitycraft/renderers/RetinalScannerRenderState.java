package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class RetinalScannerRenderState extends BlockEntityRenderState {
	public Direction facing;
	public Direction rotation;
	public RenderType renderType;
	public int normalX;
	public int normalY;
	public int normalZ;
	public int combinedLight;
}
