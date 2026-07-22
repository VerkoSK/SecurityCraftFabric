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
	private static final double REACH = 8.0;

	private NetworkHandler() {}

	/** Registers all payload codecs. Must run on both sides, so it is called from the common initializer. */
	public static void registerPayloads() {
		PayloadTypeRegistry.clientboundPlay().register(OpenKeypadScreenPayload.TYPE, OpenKeypadScreenPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetPasscodePayload.TYPE, SetPasscodePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CheckPasscodePayload.TYPE, CheckPasscodePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncBlockReinforcerPayload.TYPE, SyncBlockReinforcerPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncLaserSideConfigPayload.TYPE, SyncLaserSideConfigPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateLaserColorsPayload.TYPE, UpdateLaserColorsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetListModuleDataPayload.TYPE, SetListModuleDataPayload.CODEC);
	}

	/** Registers the server-side handlers for the client -> server passcode packets. */
	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(SetPasscodePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(CheckPasscodePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleCheckPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncBlockReinforcerPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncBlockReinforcer(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncLaserSideConfigPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncLaserSideConfig(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetListModuleDataPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetListModuleData(player, payload));
		});
	}

	private static void handleSetListModuleData(ServerPlayer player, SetListModuleDataPayload payload) {
		net.minecraft.world.item.ItemStack stack = net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, net.geforcemods.securitycraft.SCContent.ALLOWLIST_MODULE);

		if (stack.isEmpty())
			stack = net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, net.geforcemods.securitycraft.SCContent.DENYLIST_MODULE);

		if (!player.isSpectator() && !stack.isEmpty()) {
			net.geforcemods.securitycraft.components.ListModuleData data = payload.listModuleData();

			stack.set(net.geforcemods.securitycraft.SCContent.LIST_MODULE_DATA, new net.geforcemods.securitycraft.components.ListModuleData(data.players().stream().distinct().toList(), data.teams().stream().filter(player.level().getScoreboard().getTeamNames()::contains).toList(), data.affectEveryone()));
		}
	}

	private static void handleSyncLaserSideConfig(ServerPlayer player, SyncLaserSideConfigPayload payload) {
		ServerLevel level = player.level();

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

		if (player.level().getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.PasscodeProtected keypad && keypad.getOwner().isOwner(player)) {
			keypad.setPasscode(payload.passcode());
			player.sendOverlayMessage(Component.translatable("messages.securitycraft:passcode.set"));
		}
	}

	private static void handleCheckPasscode(ServerPlayer player, CheckPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.level().getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.PasscodeProtected keypad && keypad.hasPasscode() && !keypad.isOnCooldown()) {
			if (keypad.checkPasscode(payload.passcode())) {
				keypad.activate((ServerLevel) player.level());
				player.sendOverlayMessage(Component.translatable("messages.securitycraft:passcode.correct"));
			}
			else {
				keypad.onIncorrectPasscodeEntered(player);
				player.sendOverlayMessage(Component.translatable("messages.securitycraft:passcode.incorrect"));
			}
		}
	}

	private static boolean validPasscode(String passcode) {
		return passcode != null && !passcode.isBlank();
	}

	private static boolean inReach(ServerPlayer player, BlockPos pos) {
		return pos.closerThan(player.blockPosition(), REACH);
	}
}
