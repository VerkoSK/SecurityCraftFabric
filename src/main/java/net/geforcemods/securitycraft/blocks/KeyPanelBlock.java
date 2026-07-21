package net.geforcemods.securitycraft.blocks;

import com.mojang.serialization.MapCodec;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeyPanelBlockEntity;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin wall/floor/ceiling passcode panel that emits a redstone pulse when the correct code is entered. */
public class KeyPanelBlock extends AbstractPanelBlock {
	public static final MapCodec<KeyPanelBlock> CODEC = simpleCodec(KeyPanelBlock::new);
	public static final VoxelShape FLOOR_NS = Block.box(2.0D, 0.0D, 1.0D, 14.0D, 1.0D, 15.0D);
	public static final VoxelShape FLOOR_EW = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 1.0D, 14.0D);
	public static final VoxelShape CEILING_NS = Block.box(2.0D, 15.0D, 1.0D, 14.0D, 16.0D, 15.0D);
	public static final VoxelShape CEILING_EW = Block.box(1.0D, 15.0D, 2.0D, 15.0D, 16.0D, 14.0D);
	public static final VoxelShape WALL_N = Block.box(2.0D, 1.0D, 15.0D, 14.0D, 15.0D, 16.0D);
	public static final VoxelShape WALL_E = Block.box(0.0D, 1.0D, 2.0D, 1.0D, 15.0D, 14.0D);
	public static final VoxelShape WALL_S = Block.box(2.0D, 1.0D, 0.0D, 14.0D, 15.0D, 1.0D);
	public static final VoxelShape WALL_W = Block.box(15.0D, 1.0D, 2.0D, 16.0D, 15.0D, 14.0D);

	public KeyPanelBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(POWERED))
			return InteractionResult.PASS;

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (!(level.getBlockEntity(pos) instanceof KeyPanelBlockEntity be) || !(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.PASS;

		Owner owner = be.getOwner();

		if (!owner.owns()) {
			be.setOwner(player.getName().getString(), player.getUUID().toString());
			NetworkHandler.openKeypadScreen(serverPlayer, pos, true, player.getName().getString());
		}
		else if (owner.isOwner(player) && player.isShiftKeyDown())
			NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
		else if (!be.hasPasscode()) {
			if (owner.isOwner(player))
				NetworkHandler.openKeypadScreen(serverPlayer, pos, true, owner.getName());
			else
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.notSetUp"), true);
		}
		else
			NetworkHandler.openKeypadScreen(serverPlayer, pos, false, owner.getName());

		return InteractionResult.CONSUME;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACE)) {
			case FLOOR -> switch (state.getValue(FACING)) {
				case NORTH, SOUTH -> FLOOR_NS;
				case EAST, WEST -> FLOOR_EW;
				default -> Shapes.empty();
			};
			case CEILING -> switch (state.getValue(FACING)) {
				case NORTH, SOUTH -> CEILING_NS;
				case EAST, WEST -> CEILING_EW;
				default -> Shapes.empty();
			};
			case WALL -> switch (state.getValue(FACING)) {
				case NORTH -> WALL_N;
				case EAST -> WALL_E;
				case SOUTH -> WALL_S;
				case WEST -> WALL_W;
				default -> Shapes.empty();
			};
		};
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeyPanelBlockEntity(pos, state);
	}
}
