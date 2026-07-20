package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Registers SecurityCraft's payload types and the server-side receivers. */
public final class NetworkHandler {
	private static final int MAX_PASSCODE_LENGTH = 8;
	private static final double REACH = 8.0;

	private NetworkHandler() {}

	/** Registers all payload codecs. Must run on both sides, so it is called from the common initializer. */
	public static void registerPayloads() {
		PayloadTypeRegistry.playS2C().register(OpenKeypadScreenPayload.TYPE, OpenKeypadScreenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetPasscodePayload.TYPE, SetPasscodePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CheckPasscodePayload.TYPE, CheckPasscodePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncBlockReinforcerPayload.TYPE, SyncBlockReinforcerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncLaserSideConfigPayload.TYPE, SyncLaserSideConfigPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateLaserColorsPayload.TYPE, UpdateLaserColorsPayload.CODEC);
	}

	/** Registers the server-side handlers for the client -> server passcode packets. */
	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(SetPasscodePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();

			if (server != null)
				server.execute(() -> handleSetPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(CheckPasscodePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();

			if (server != null)
				server.execute(() -> handleCheckPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncBlockReinforcerPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();

			if (server != null)
				server.execute(() -> handleSyncBlockReinforcer(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncLaserSideConfigPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();

			if (server != null)
				server.execute(() -> handleSyncLaserSideConfig(player, payload));
		});
	}

	private static void handleSyncLaserSideConfig(ServerPlayer player, SyncLaserSideConfigPayload payload) {
		ServerLevel level = player.serverLevel();

		if (!player.isSpectator() && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity be && be.isOwnedBy(player)) {
			net.minecraft.world.level.block.state.BlockState state = level.getBlockState(payload.pos());

			be.applyNewSideConfig(net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity.loadSideConfig(payload.sideConfig()), player);
			level.sendBlockUpdated(payload.pos(), state, state, 2);
		}
	}

	private static void handleSyncBlockReinforcer(ServerPlayer player, SyncBlockReinforcerPayload payload) {
		net.minecraft.world.item.ItemStack held = player.getMainHandItem().getItem() instanceof net.geforcemods.securitycraft.items.BlockReinforcerItem ? player.getMainHandItem() : player.getOffhandItem();

		if (held.getItem() instanceof net.geforcemods.securitycraft.items.BlockReinforcerItem item && item.canToggleMode(held))
			net.geforcemods.securitycraft.items.BlockReinforcerItem.setReinforcing(held, payload.isReinforcing());
	}

	public static void openKeypadScreen(ServerPlayer player, BlockPos pos, boolean setup, String ownerName) {
		ServerPlayNetworking.send(player, new OpenKeypadScreenPayload(pos, setup, ownerName));
	}

	private static void handleSetPasscode(ServerPlayer player, SetPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.level().getBlockEntity(payload.pos()) instanceof KeypadBlockEntity keypad && keypad.getOwner().isOwner(player)) {
			keypad.setPasscode(payload.passcode());
			player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.set"), true);
		}
	}

	private static void handleCheckPasscode(ServerPlayer player, CheckPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.level().getBlockEntity(payload.pos()) instanceof KeypadBlockEntity keypad && keypad.hasPasscode()) {
			if (keypad.checkPasscode(payload.passcode())) {
				keypad.activate((ServerLevel) player.level());
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.correct"), true);
			}
			else
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.incorrect"), true);
		}
	}

	private static boolean validPasscode(String passcode) {
		return passcode != null && !passcode.isBlank() && passcode.length() <= MAX_PASSCODE_LENGTH;
	}

	private static boolean inReach(ServerPlayer player, BlockPos pos) {
		return pos.closerThan(player.blockPosition(), REACH);
	}
}
