package net.geforcemods.securitycraft.blocks;

import com.mojang.serialization.MapCodec;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.util.IBlockMine;
import net.geforcemods.securitycraft.util.OwnershipUtils;
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
 * Base for owned blocks. On placement the placer becomes the owner, and only the owner can break the block.
 *
 * <p>The block itself is registered as unbreakable, exactly like upstream: its destroy time is forced to
 * {@code -1.0F} (bedrock) and the real one is kept in {@link #destroyTimeForOwner}, then handed back inside
 * {@link #getDestroyProgress} only for whoever is allowed to mine it. Doing it this way rather than simply
 * returning a progress of zero also stops the vanilla client from predicting an instant break, and it makes
 * the block genuinely immune to anything that reads the block's hardness.
 *
 * <p>Fabric adaptation: upstream posts an {@code OwnershipEvent} whose handler just calls {@code setOwner},
 * and reaches the private {@code destroySpeed} field through an access transformer. Fabric has neither, so
 * the owner-set is inlined and the swap is done on a copied {@link BlockState} instead.
 */
public class OwnableBlock extends BaseEntityBlock {
	private static float destroyTimeTempStorage = -1.0F;
	protected final float destroyTimeForOwner;

	public OwnableBlock(BlockBehaviour.Properties properties) {
		super(withReinforcedDestroyTime(properties));
		destroyTimeForOwner = getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		//block mines are meant to be broken (and to then detonate) by trespassers, so they bypass the ownership
		//gate entirely, just like upstream's BlockUtils#getDestroyProgress does via its isBlockMine check
		if (this instanceof IBlockMine)
			return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos, true);

		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof Player player && level.getBlockEntity(pos) instanceof IOwnable ownable)
			ownable.setOwner(player.getGameProfile().name(), player.getUUID().toString());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return null;
	}

	/** Registers the block as unbreakable while remembering the destroy time it asked for. */
	public static BlockBehaviour.Properties withReinforcedDestroyTime(BlockBehaviour.Properties properties) {
		destroyTimeTempStorage = properties.destroyTime;
		return properties.destroyTime(-1.0F);
	}

	public static float getStoredDestroyTime() {
		float stored = destroyTimeTempStorage;

		destroyTimeTempStorage = -1.0F;
		return stored;
	}
}
