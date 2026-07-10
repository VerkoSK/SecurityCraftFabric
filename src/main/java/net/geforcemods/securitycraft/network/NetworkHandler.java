package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Registers SecurityCraft's payload types and the server-side receivers. */
public final class NetworkHandler {
	private static final int MAX_PASSCODE_LENGTH = 8;
	private static final double REACH = 8.0;

	private NetworkHandler() {}

	public static void registerPayloads() {
		PayloadTypeRegistry.playS2C().register(OpenKeypadScreenPayload.ID, OpenKeypadScreenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetPasscodePayload.ID, SetPasscodePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CheckPasscodePayload.ID, CheckPasscodePayload.CODEC);
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(SetPasscodePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			MinecraftServer server = player.getEntityWorld().getServer();

			if (server != null)
				server.execute(() -> handleSetPasscode(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(CheckPasscodePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			MinecraftServer server = player.getEntityWorld().getServer();

			if (server != null)
				server.execute(() -> handleCheckPasscode(player, payload));
		});
	}

	public static void openKeypadScreen(ServerPlayerEntity player, BlockPos pos, boolean setup, String ownerName) {
		ServerPlayNetworking.send(player, new OpenKeypadScreenPayload(pos, setup, ownerName));
	}

	private static void handleSetPasscode(ServerPlayerEntity player, SetPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.getEntityWorld().getBlockEntity(payload.pos()) instanceof KeypadBlockEntity keypad && keypad.getOwner().isOwner(player)) {
			keypad.setPasscode(payload.passcode());
			player.sendMessage(Text.translatable("messages.securitycraft:passcode.set"), true);
		}
	}

	private static void handleCheckPasscode(ServerPlayerEntity player, CheckPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		if (player.getEntityWorld().getBlockEntity(payload.pos()) instanceof KeypadBlockEntity keypad && keypad.hasPasscode()) {
			if (keypad.checkPasscode(payload.passcode())) {
				keypad.activate((ServerWorld) player.getEntityWorld());
				player.sendMessage(Text.translatable("messages.securitycraft:passcode.correct"), true);
			}
			else
				player.sendMessage(Text.translatable("messages.securitycraft:passcode.incorrect"), true);
		}
	}

	private static boolean validPasscode(String passcode) {
		return passcode != null && !passcode.isBlank() && passcode.length() <= MAX_PASSCODE_LENGTH;
	}

	private static boolean inReach(ServerPlayerEntity player, BlockPos pos) {
		return pos.isWithinDistance(player.getBlockPos(), REACH);
	}
}
