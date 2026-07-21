package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Frame block entity. Upstream this drives a live camera feed; this port keeps only the owner
 * (the camera subsystem is not ported), which is what the Key Panel conversion to a keypad needs.
 */
public class FrameBlockEntity extends OwnableBlockEntity {
	public FrameBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.FRAME_BLOCK_ENTITY, pos, state);
	}
}
