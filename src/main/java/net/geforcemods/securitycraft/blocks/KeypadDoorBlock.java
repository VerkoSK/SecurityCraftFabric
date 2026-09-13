package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadDoorBlockEntity;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Reinforced Iron Door with a keypad on it: opens only when the right code is entered, and — like every
 * reinforced block — can only be mined by whoever placed it. The passcode, modules and owner all live on the
 * lower half's block entity; interacting with either half opens the same screen.
 */
public class KeypadDoorBlock extends DoorBlock implements EntityBlock {
	private final float destroyTimeForOwner;

	public KeypadDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), type);
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	private static BlockPos lowerPos(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockPos codePos = lowerPos(pos, state);

		if (!(level.getBlockEntity(codePos) instanceof KeypadDoorBlockEntity be))
			return InteractionResult.PASS;

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (be.isDisabled())
				player.displayClientMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			else if (verifyPasscodeSet(codePos, be, serverPlayer)) {
				if (be.isDenied(player)) {
					if (be.sendsDenylistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
				}
				else if (be.isAllowed(player)) {
					if (be.sendsAllowlistMessage())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

					activate(level, codePos, be.getSignalLength());
				}
				else
					NetworkHandler.openKeypadScreen(serverPlayer, codePos, false, be.getOwner().getName());
			}
		}

		return InteractionResult.SUCCESS;
	}

	private boolean verifyPasscodeSet(BlockPos codePos, KeypadDoorBlockEntity be, ServerPlayer player) {
		if (be.hasPasscode())
			return true;

		Owner owner = be.getOwner();

		if (owner.isOwner(player))
			NetworkHandler.openKeypadScreen(player, codePos, true, owner.getName());
		else
			PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);

		return false;
	}

	/** Toggles both halves of the door at {@code lowerPos} and plays the door sound, bypassing redstone. */
	public void activate(Level level, BlockPos lowerPos, int signalLength) {
		BlockState lower = level.getBlockState(lowerPos);

		if (!(lower.getBlock() instanceof KeypadDoorBlock))
			return;

		boolean open = !lower.getValue(OPEN);

		level.setBlock(lowerPos, lower.setValue(OPEN, open), 2);

		BlockState upper = level.getBlockState(lowerPos.above());

		if (upper.getBlock() instanceof KeypadDoorBlock)
			level.setBlock(lowerPos.above(), upper.setValue(OPEN, open), 2);

		level.playSound(null, lowerPos, open ? type().doorOpen() : type().doorClose(), SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, lowerPos);
		level.updateNeighborsAt(lowerPos, this);

		if (open && signalLength > 0)
			level.scheduleTick(lowerPos, this, signalLength);
	}

	@Override
	public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
		BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
		BlockState lower = level.getBlockState(lowerPos);

		if (lower.getBlock() instanceof KeypadDoorBlock && lower.getValue(OPEN))
			activate(level, lowerPos, 0);
	}

	/** The keypad door only opens on a correct code; a redstone signal must not open it the way a vanilla door would. */
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean movedByPiston) {}

	/** Placing next to a redstone source must not place the door already open. */
	@Override
	public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
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
		OwnershipUtils.setPlacedBy(level, pos.above(), placer);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (player.isCreative() && level.getBlockEntity(lowerPos(pos, state)) instanceof IModuleInventory inv)
			inv.getInventory().clear();

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER && level.getBlockEntity(pos) instanceof IModuleInventory inv)
			inv.dropAllModules();

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadDoorBlockEntity(pos, state);
	}
}
