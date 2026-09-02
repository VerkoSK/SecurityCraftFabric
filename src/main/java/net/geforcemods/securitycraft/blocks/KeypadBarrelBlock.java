package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasscodeConvertible;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadBarrelBlockEntity;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The keypad barrel: an ownable, passcode-protected barrel. Ported 1:1 from upstream's {@code KeypadBarrelBlock}
 * except for what depends on content this port lacks — see {@link KeypadBarrelBlockEntity}'s javadoc for the
 * full list (Codebreaker, Disguise module, {@code ILockable}/sentries, the {@code SaltData} salt registry).
 * Upstream extends its own {@code DisguisableBlock}; since that class doesn't exist here, this extends vanilla's
 * block behaviour directly and wires ownership in via {@link OwnershipUtils}, the same idiom
 * {@link ElectrifiedIronFenceGateBlock} already uses in this port.
 */
public class KeypadBarrelBlock extends Block implements EntityBlock {
	//upstream's DisguisableBlock superclass also implements SimpleWaterloggedBlock and carries a WATERLOGGED
	//property; since that class doesn't exist in this port, waterlogging is dropped along with it rather than
	//half-ported without its updateShape/getFluidState plumbing
	public static final EnumProperty<net.minecraft.core.Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<LidFacing> LID_FACING = EnumProperty.create("lid_facing", LidFacing.class);
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty FROG = BooleanProperty.create("frog");
	private final float destroyTimeForOwner;

	public KeypadBarrelBlock(BlockBehaviour.Properties properties) {
		super(OwnableBlock.withReinforcedDestroyTime(properties));
		destroyTimeForOwner = OwnableBlock.getStoredDestroyTime();
		registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(OPEN, false).setValue(LID_FACING, LidFacing.UP).setValue(FROG, false));
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return OwnershipUtils.getDestroyProgress(destroyTimeForOwner, state, player, level, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite()).setValue(LID_FACING, LidFacing.fromDirection(ctx.getNearestLookingDirection().getOpposite()));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
		OwnershipUtils.setPlacedBy(level, pos, entity);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		//return TRY_WITH_EMPTY_HAND, not PASS: 1.21.2+ only falls through to useWithoutItem (which opens the barrel) on the former
		if (!(level.getBlockEntity(pos) instanceof KeypadBarrelBlockEntity be))
			return InteractionResult.TRY_WITH_EMPTY_HAND;

		if (stack.is(Items.FROG_SPAWN_EGG) && be.isOwnedBy(player)) {
			if (!level.isClientSide)
				level.setBlockAndUpdate(pos, state.cycle(FROG));

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KeypadBarrelBlockEntity be))
			return InteractionResult.PASS;

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
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
				player.awardStat(Stats.CUSTOM.get(Stats.OPEN_BARREL));
			}
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		BlockEntity be = level.getBlockEntity(pos);

		if (be instanceof Container container) {
			Containers.dropContents(level, pos, container);
			level.updateNeighbourForOutputSignal(pos, this);
		}

		//replaces upstream's un-ported SCEventHandler#onBlockEventBreak generic module-drop hook, matching the
		//inline onRemove pattern this port already uses for KeypadBlock
		if (be instanceof IModuleInventory inv)
			inv.dropAllModules();

		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof KeypadBarrelBlockEntity barrel)
			barrel.recheckOpen();
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		//this port extends plain Block, not BaseEntityBlock, so the block entity is not wired up as the menu provider by default
		return level.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeypadBarrelBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, LID_FACING, OPEN, FROG);
	}

	public static class Convertible implements IPasscodeConvertible {
		@Override
		public boolean isUnprotectedBlock(BlockState state) {
			//upstream matches the "barrels/wooden" tag to catch modded wooden barrels too; this port has no such
			//tag wired up yet, so this is narrowed to vanilla's own barrel
			return state.is(Blocks.BARREL);
		}

		@Override
		public boolean isProtectedBlock(BlockState state) {
			return state.is(SCContent.KEYPAD_BARREL);
		}

		@Override
		public boolean protect(Player player, Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			BarrelBlockEntity barrel = (BarrelBlockEntity) level.getBlockEntity(pos);
			LidFacing generalFacing = LidFacing.fromDirection(state.getValue(BarrelBlock.FACING));
			Direction horizontalFacing;
			CompoundTag tag;
			KeypadBarrelBlockEntity keypadBarrel;

			barrel.unpackLootTable(player); //generate loot (if any), so items don't spill out when converting and no additional loot table is generated
			tag = barrel.saveWithFullMetadata(level.registryAccess());
			barrel.clearContent();
			horizontalFacing = switch (generalFacing) {
				case UP, DOWN -> player == null ? Direction.NORTH : player.getDirection().getOpposite();
				case SIDEWAYS -> state.getValue(BarrelBlock.FACING);
			};
			level.setBlockAndUpdate(pos, SCContent.KEYPAD_BARREL.defaultBlockState().setValue(HORIZONTAL_FACING, horizontalFacing).setValue(LID_FACING, generalFacing).setValue(OPEN, false));
			keypadBarrel = (KeypadBarrelBlockEntity) level.getBlockEntity(pos);
			keypadBarrel.loadWithComponents(tag, level.registryAccess());
			keypadBarrel.setPreviousBarrel(state.getBlock());

			if (player != null)
				keypadBarrel.setOwner(player.getName().getString(), player.getUUID().toString());

			return true;
		}

		@Override
		public boolean unprotect(Player player, Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			KeypadBarrelBlockEntity keypadBarrel = (KeypadBarrelBlockEntity) level.getBlockEntity(pos);
			LidFacing lidFacing = state.getValue(LID_FACING);
			Direction direction = switch (lidFacing) {
				case UP -> Direction.UP;
				case SIDEWAYS -> state.getValue(KeypadBarrelBlock.HORIZONTAL_FACING);
				case DOWN -> Direction.DOWN;
			};
			CompoundTag tag;
			BarrelBlockEntity barrel;
			Block convertedBlock = BuiltInRegistries.BLOCK.getValue(keypadBarrel.getPreviousBarrel());

			if (convertedBlock == Blocks.AIR)
				convertedBlock = Blocks.BARREL;

			keypadBarrel.dropAllModules();
			keypadBarrel.unpackLootTable(player); //generate loot (if any), so items don't spill out when converting and no additional loot table is generated
			tag = keypadBarrel.saveWithFullMetadata(level.registryAccess());
			keypadBarrel.clearContent();
			level.setBlockAndUpdate(pos, convertedBlock.defaultBlockState().setValue(BarrelBlock.FACING, direction).setValue(OPEN, false));
			barrel = (BarrelBlockEntity) level.getBlockEntity(pos);
			barrel.loadWithComponents(tag, level.registryAccess());
			return true;
		}
	}

	public enum LidFacing implements StringRepresentable {
		UP("up"),
		SIDEWAYS("sideways"),
		DOWN("down");

		private final String name;

		private LidFacing(String name) {
			this.name = name;
		}

		public static LidFacing fromDirection(Direction direction) {
			return switch (direction) {
				case UP -> UP;
				case NORTH, SOUTH, EAST, WEST -> SIDEWAYS;
				case DOWN -> DOWN;
			};
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
