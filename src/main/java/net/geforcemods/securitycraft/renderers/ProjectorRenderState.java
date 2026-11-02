package net.geforcemods.securitycraft.renderers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProjectorRenderState extends BlockEntityRenderState {
	public boolean active = false;
	public BlockState projectedState = null;
	public net.minecraft.client.renderer.block.MovingBlockRenderState movingBlock = null;
	public final List<BlockPos> relativePositions = new ArrayList<>();
}
