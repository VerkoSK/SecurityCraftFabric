package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Ported from upstream's {@code SecretStandingSignBlock}. {@code canHarvestBlock} (Forge-only, only served
 * the un-ported {@code alwaysDrop} config check) is dropped, matching the idiom already used across this
 * port's reinforced blocks (see {@code ReinforcedHopperBlock}). Ownership is wired in by hand via
 * {@link OwnershipUtils} since this must extend vanilla's {@link StandingSignBlock}.
 */
public class SecretStandingSignBlock extends StandingSignBlock {
	private final float destroyTimeForOwner;

	public SecretStandingSignBlock(BlockBehaviour.Properties properties, WoodType woodType) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), woodType);
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		//prevents dropping twice the amount of modules when breaking the block in creative mode
		if (player.isCreative() && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.getInventory().clear();

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		//upstream also gates this on !ConfigHandler.SERVER.vanillaToolBlockBreaking.get(), an un-ported config
		//option; dropped here to match the shouldDropModules-only idiom already used by KeypadChestBlock
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof IModuleInventory inv && inv.shouldDropModules())
			inv.dropAllModules();

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof SecretSignBlockEntity be && (be.isOwnedBy(player) || be.isAllowed(player)))
			return super.use(state, level, pos, player, hand, hit);

		return InteractionResult.PASS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SecretSignBlockEntity(pos, state);
	}

	@Override
	public String getDescriptionId() {
		return super.getDescriptionId().replace("_standing", "");
	}
}
