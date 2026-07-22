package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Registers SecurityCraft's channels and the server-side receivers. On MC 1.20.1 channels are implicit (no payload-type registry). */
public final class NetworkHandler {
	private static final double REACH = 8.0;

	private NetworkHandler() {}

	/** No-op on 1.20.1 (channels are registered implicitly when a receiver is added). Kept for a stable init sequence. */
	public static void registerPayloads() {}

	/** Registers the server-side handlers for the client -> server packets. */
	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(SetPasscodePayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetPasscodePayload payload = SetPasscodePayload.read(buf);

			server.execute(() -> handleSetPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(CheckPasscodePayload.CHANNEL, (server, player, handler, buf, sender) -> {
			CheckPasscodePayload payload = CheckPasscodePayload.read(buf);

			server.execute(() -> handleCheckPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncBlockReinforcerPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncBlockReinforcerPayload payload = SyncBlockReinforcerPayload.read(buf);

			server.execute(() -> handleSyncBlockReinforcer(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncLaserSideConfigPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncLaserSideConfigPayload payload = SyncLaserSideConfigPayload.read(buf);

			server.execute(() -> handleSyncLaserSideConfig(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetListModuleDataPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetListModuleDataPayload payload = SetListModuleDataPayload.read(buf);

			server.execute(() -> handleSetListModuleData(player, payload));
		});
	}

	private static void handleSetListModuleData(ServerPlayer player, SetListModuleDataPayload payload) {
		ItemStack stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.ALLOWLIST_MODULE);

		if (stack.isEmpty())
			stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.DENYLIST_MODULE);

		if (!player.isSpectator() && !stack.isEmpty()) {
			ListModuleData data = payload.listModuleData();

			new ListModuleData(data.players().stream().distinct().toList(), data.teams().stream().filter(player.getScoreboard().getTeamNames()::contains).toList(), data.affectEveryone()).writeToStack(stack);
		}
	}

	private static void handleSyncLaserSideConfig(ServerPlayer player, SyncLaserSideConfigPayload payload) {
		ServerLevel level = player.serverLevel();

		if (!player.isSpectator() && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity be && be.isOwnedBy(player)) {
			BlockState state = level.getBlockState(payload.pos());

			be.applyNewSideConfig(net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity.loadSideConfig(payload.sideConfig()), player);
			level.sendBlockUpdated(payload.pos(), state, state, 2);
		}
	}

	private static void handleSyncBlockReinforcer(ServerPlayer player, SyncBlockReinforcerPayload payload) {
		ItemStack held = player.getMainHandItem().getItem() instanceof net.geforcemods.securitycraft.items.BlockReinforcerItem ? player.getMainHandItem() : player.getOffhandItem();

		if (held.getItem() instanceof net.geforcemods.securitycraft.items.BlockReinforcerItem item && item.canToggleMode(held))
			net.geforcemods.securitycraft.items.BlockReinforcerItem.setReinforcing(held, payload.isReinforcing());
	}

	public static void openKeypadScreen(ServerPlayer player, BlockPos pos, boolean setup, String ownerName) {
		ServerPlayNetworking.send(player, OpenKeypadScreenPayload.CHANNEL, new OpenKeypadScreenPayload(pos, setup, ownerName).write());
	}

	private static void handleSetPasscode(ServerPlayer player, SetPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.level().getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.PasscodeProtected keypad && keypad.getOwner().isOwner(player)) {
			keypad.setPasscode(payload.passcode());
			player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.set"), true);
		}
	}

	private static void handleCheckPasscode(ServerPlayer player, CheckPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.level().getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.PasscodeProtected keypad && keypad.hasPasscode() && !keypad.isOnCooldown()) {
			if (keypad.checkPasscode(payload.passcode())) {
				keypad.activate((ServerLevel) player.level());
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.correct"), true);
			}
			else {
				keypad.onIncorrectPasscodeEntered(player);
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.incorrect"), true);
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
