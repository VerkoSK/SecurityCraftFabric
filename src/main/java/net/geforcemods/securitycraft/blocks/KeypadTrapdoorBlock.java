package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadTrapdoorBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Reinforced Iron Trapdoor with a keypad on it: opens only when the right code is entered, and — like every
 * reinforced block — can only be mined by whoever placed it. Redstone cannot open it.
 */
public class KeypadTrapdoorBlock extends TrapDoorBlock implements EntityBlock {
	private final float destroyTimeForOwner;
	private final BlockSetType blockSetType;

	public KeypadTrapdoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), type);
		blockSetType = type;
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeypadTrapdoorBlockEntity be))
			return InteractionResult.PASS;

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (be.isDisabled())
				player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			else if (verifyPasscodeSet(pos, be, serverPlayer)) {
				if (be.isDenied(player)) {
					if (be.sendsDenylistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
				}
				else if (be.isAllowed(player)) {
					if (be.sendsAllowlistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

					activate(level, pos);
				}
				else
					NetworkHandler.openKeypadScreen(serverPlayer, pos, false, be.getOwner().getName());
			}
		}

		return InteractionResult.SUCCESS;
	}

	private boolean verifyPasscodeSet(BlockPos pos, KeypadTrapdoorBlockEntity be, ServerPlayer player) {
		if (be.hasPasscode())
			return true;

		Owner owner = be.getOwner();

		if (owner.isOwner(player))
			NetworkHandler.openKeypadScreen(player, pos, true, owner.getName());
		else
			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);

		return false;
	}

	/** Toggles the trapdoor and plays its sound, bypassing redstone. */
	public void activate(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof KeypadTrapdoorBlock))
			return;

		boolean open = !state.getValue(OPEN);

		level.setBlock(pos, state.setValue(OPEN, open), 2);
		level.playSound(null, pos, open ? blockSetType.trapdoorOpen() : blockSetType.trapdoorClose(), SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
		level.updateNeighborsAt(pos, this);
	}

	/** Redstone must not open the keypad trapdoor the way it opens a vanilla one. */
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {}

	/** Placing next to a redstone source must not place it already open. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);

		return state == null ? null : state.setValue(OPEN, false).setValue(POWERED, false);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		OwnershipUtils.setPlacedBy(level, pos, placer);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (player.isCreative() && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.getInventory().clear();

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.dropAllModules();

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadTrapdoorBlockEntity(pos, state);
	}
}
