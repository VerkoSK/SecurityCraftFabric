package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class SecurityCameraRenderState extends BlockEntityRenderState {
	public boolean shouldSkip;
	public boolean isDisguised;
	public Direction facing = Direction.NORTH;
	public float cameraRotation;
	public boolean isShutDown;
	public boolean hasColor;
	public int color;
	public ResourceLocation texture;
}
