package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Both halves of a reinforced door carry one of these, exactly like upstream: an owner on the lower half alone
 * would leave the upper half ownerless, which makes it breakable by anyone and leaves Jade with nothing to show
 * when the crosshair is on it. Setting the owner on one half sets it on the other, which is what upstream's
 * {@code onOwnerChanged} does.
 */
public class ReinforcedDoorBlockEntity extends OwnableBlockEntity {
	public ReinforcedDoorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.REINFORCED_DOOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void setOwner(String name, String uuid) {
		super.setOwner(name, uuid);

		if (level == null)
			return;

		BlockState state = getBlockState();

		if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
			return;

		BlockPos otherPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? worldPosition.below() : worldPosition.above();

		if (level.getBlockEntity(otherPos) instanceof ReinforcedDoorBlockEntity otherHalf) {
			Owner otherOwner = otherHalf.getOwner();

			//without this guard the two halves would keep setting each other's owner forever
			if (!otherOwner.getName().equals(name) || !otherOwner.getUUID().equals(uuid))
				otherHalf.setOwner(name, uuid);
		}
	}
}
