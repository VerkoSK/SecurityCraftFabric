package net.geforcemods.securitycraft.blocks;

import java.util.Optional;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasscodeConvertible;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.OwnershipUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The keypad chest: an ownable, passcode-protected {@link ChestBlock}. Ported 1:1 from upstream's
 * {@code KeypadChestBlock}, minus everything that leans on content this port doesn't have: the Codebreaker item
 * (its bypass check in {@link #use}), the Disguise module and the whole {@code IDisguisable}/{@code IOverlayDisplay}
 * appearance surface it drove (shape/light/sound/appearance overrides, pick-block disguise stack), and
 * {@code ConfigHandler.SERVER.alwaysDrop} (no such option exists in this port's config, so {@code canHarvestBlock}
 * is not overridden and falls back to vanilla). Can't extend {@link OwnableBlock} (single inheritance forces this to
 * extend vanilla's {@link ChestBlock}), so ownership is wired in by hand via {@link OwnershipUtils}, matching the
 * idiom already used by {@link ElectrifiedIronFenceGateBlock} in this port.
 */
public class KeypadChestBlock extends ChestBlock {
	private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> CONTAINER_MERGER = new DoubleBlockCombiner.Combiner<>() {
		@Override
		public Optional<MenuProvider> acceptDouble(final ChestBlockEntity chest1, final ChestBlockEntity chest2) {
			final Container chestInventory = new CompoundContainer(chest1, chest2);
			return Optional.of(new MenuProvider() {
				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
					if (chest1.canOpen(player) && chest2.canOpen(player)) {
						chest1.unpackLootTable(inventory.player);
						chest2.unpackLootTable(inventory.player);
						return ChestMenu.sixRows(id, inventory, chestInventory);
					}
					else
						return null;
				}

				@Override
				public Component getDisplayName() {
					if (chest1.hasCustomName())
						return chest1.getDisplayName();
					else
						return chest2.hasCustomName() ? chest2.getDisplayName() : Utils.localize("block.securitycraft.keypad_chest_double");
				}
			});
		}

		@Override
		public Optional<MenuProvider> acceptSingle(ChestBlockEntity be) {
			return Optional.of(be);
		}

		@Override
		public Optional<MenuProvider> acceptNone() {
			return Optional.empty();
		}
	};
	private final float destroyTimeForOwner;

	public KeypadChestBlock(BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties), () -> SCContent.KEYPAD_CHEST_BLOCK_ENTITY);
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeypadChestBlockEntity be))
			return InteractionResult.PASS;

		if (!level.isClientSide && !isBlocked(level, pos) && player instanceof ServerPlayer serverPlayer) {
			if (be.isDenied(player)) {
				if (be.sendsDenylistMessage())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onDenylist"), ChatFormatting.RED);
			}
			else if (be.isAllowed(player)) {
				if (be.sendsAllowlistMessage())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getDescriptionId()), Utils.localize("messages.securitycraft:module.onAllowlist"), ChatFormatting.GREEN);

				activate(state, level, pos, player);
			}
			else if (!be.hasPasscode()) {
				Owner owner = be.getOwner();

				if (owner.isOwner(player))
					NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
				else
					PlayerUtils.sendMessageToPlayer(player, Component.literal("SecurityCraft"), Utils.localize("messages.securitycraft:passcodeProtected.notSetUp"), ChatFormatting.DARK_RED);
			}
			else {
				//Codebreaker item not ported, so there is no bypass check here like upstream has
				be.setPendingOpener(player);
				NetworkHandler.openKeypadScreen(serverPlayer, pos, false, be.getOwner().getName());
			}
		}

		return InteractionResult.SUCCESS;
	}

	public void activate(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide) {
			MenuProvider menuProvider = getMenuProvider(state, level, pos);

			if (menuProvider != null) {
				player.openMenu(menuProvider);
				player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
			}
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (entity instanceof Player player) {
			OwnershipUtils.setPlacedBy(level, pos, entity);

			if (state.getValue(TYPE) != ChestType.SINGLE) {
				KeypadChestBlockEntity thisBe = (KeypadChestBlockEntity) level.getBlockEntity(pos);
				BlockEntity otherBe = level.getBlockEntity(pos.relative(getConnectedDirection(state)));

				if (otherBe instanceof KeypadChestBlockEntity be && thisBe.getOwner().owns(be))
					thisBe.mergeFrom(be);
			}
		}
	}

	@Override
	public Direction candidatePartnerFacing(BlockPlaceContext ctx, Direction dir) {
		Direction returnValue = super.candidatePartnerFacing(ctx, dir);

		//only connect to chests which have the same owner
		if (returnValue != null && ctx.getLevel().getBlockEntity(ctx.getClickedPos().relative(dir)) instanceof IOwnable ownable && ownable.isOwnedBy(ctx.getPlayer()))
			return returnValue;

		return null;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		if (level.getBlockEntity(pos) instanceof KeypadChestBlockEntity be)
			return be.isModuleEnabled(ModuleType.REDSTONE) ? Mth.clamp(be.getNumPlayersUsing(), 0, 15) : 0;
		else
			return 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return side == Direction.UP ? state.getSignal(level, pos, side) : 0;
	}

	//upstream also overrides Forge's onNeighborChange to refresh the block entity's cached state; vanilla has no
	//such hook, and neighborChanged below already covers the case this port needs

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		//replaces upstream's un-ported SCEventHandler#onBlockEventBreak generic module-drop hook, matching the
		//inline onRemove pattern this port already uses for KeypadBlock
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof IModuleInventory inv && inv.shouldDropModules())
			inv.dropAllModules();

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return combine(state, level, pos, false).apply(CONTAINER_MERGER).orElse(null);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadChestBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? createTickerHelper(type, SCContent.KEYPAD_CHEST_BLOCK_ENTITY, KeypadChestBlockEntity::lidAnimateTick) : null;
	}

	public static boolean isBlocked(Level level, BlockPos pos) {
		return isBelowSolidBlock(level, pos);
	}

	private static boolean isBelowSolidBlock(Level level, BlockPos pos) {
		return level.getBlockState(pos.above()).isRedstoneConductor(level, pos.above());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		//like every chest, the block model is empty and the chest itself is drawn by its block entity renderer
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	public static class Convertible implements IPasscodeConvertible {
		@Override
		public boolean isUnprotectedBlock(BlockState state) {
			//upstream matches the "chests/wooden" tag to catch modded wooden chests too; this port has no such
			//tag wired up yet, so this is narrowed to vanilla's own chest
			return state.is(Blocks.CHEST);
		}

		@Override
		public boolean isProtectedBlock(BlockState state) {
			return state.is(SCContent.KEYPAD_CHEST);
		}

		@Override
		public boolean protect(Player player, Level level, BlockPos pos) {
			convert(player, level, pos, true);
			return true;
		}

		@Override
		public boolean unprotect(Player player, Level level, BlockPos pos) {
			convert(player, level, pos, false);
			return true;
		}

		@Override
		public int getRequiredKeyPanels(BlockState state) {
			return state.getValue(TYPE) != ChestType.SINGLE ? 2 : 1;
		}

		private void convert(Player player, Level level, BlockPos pos, boolean protect) {
			BlockState state = level.getBlockState(pos);
			Direction facing = state.getValue(FACING);
			ChestType type = state.getValue(TYPE);
			ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(pos);

			if (!protect)
				((IModuleInventory) chest).dropAllModules();

			convertSingleChest(chest, player, level, pos, state, facing, type, protect);

			if (type != ChestType.SINGLE) {
				BlockPos newPos = pos.relative(getConnectedDirection(state));
				BlockState newState = level.getBlockState(newPos);

				convertSingleChest((ChestBlockEntity) level.getBlockEntity(newPos), player, level, newPos, newState, facing, type.getOpposite(), protect);
			}
		}

		private void convertSingleChest(ChestBlockEntity chest, Player player, Level level, BlockPos pos, BlockState oldChestState, Direction facing, ChestType type, boolean protect) {
			CompoundTag tag;
			Block convertedBlock;

			if (protect)
				convertedBlock = SCContent.KEYPAD_CHEST;
			else {
				convertedBlock = BuiltInRegistries.BLOCK.get(((KeypadChestBlockEntity) chest).getPreviousChest());

				if (convertedBlock == Blocks.AIR)
					convertedBlock = Blocks.CHEST;
			}

			chest.unpackLootTable(player); //generate loot (if any), so items don't spill out when converting and no additional loot table is generated
			tag = chest.saveWithFullMetadata(level.registryAccess());
			chest.clearContent();
			level.setBlockAndUpdate(pos, convertedBlock.defaultBlockState().setValue(FACING, facing).setValue(TYPE, type));
			chest = (ChestBlockEntity) level.getBlockEntity(pos);
			chest.loadWithComponents(tag, level.registryAccess());

			if (protect) {
				if (player != null)
					((IOwnable) chest).setOwner(player.getName().getString(), player.getUUID().toString());

				((KeypadChestBlockEntity) chest).setPreviousChest(oldChestState.getBlock());
			}
		}
	}
}
