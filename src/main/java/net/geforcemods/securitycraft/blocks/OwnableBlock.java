package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base for owned blocks. On placement the placer becomes the owner; only the owner can break the block.
 *
 * <p>Fabric adaptation: the upstream posts a {@code OwnershipEvent} (its handler just calls {@code setOwner})
 * and toggles the private {@code destroySpeed} field via an access-transformer to make non-owners unable to
 * break it. Fabric has neither, so the owner-set is inlined and owner-only breaking is done by returning a
 * destroy progress of 0 for non-owners (the config-based breaking-speed tuning is not ported).
 */
public class OwnableBlock extends BaseEntityBlock {
	public OwnableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IOwnable ownable && ownable.getOwner().owns() && !ownable.isOwnedBy(player))
			return 0.0F;

		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof Player player && level.getBlockEntity(pos) instanceof IOwnable ownable)
			ownable.setOwner(player.getGameProfile().getName(), player.getUUID().toString());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}
}
