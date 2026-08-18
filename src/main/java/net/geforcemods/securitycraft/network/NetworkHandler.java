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
		PayloadTypeRegistry.serverboundPlay().register(RemoteControlMinePayload.TYPE, RemoteControlMinePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RemoveMineFromMRATPayload.TYPE, RemoveMineFromMRATPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetOptionPayload.TYPE, SetOptionPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ToggleModulePayload.TYPE, ToggleModulePayload.CODEC);
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
		ServerPlayNetworking.registerGlobalReceiver(RemoteControlMinePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleRemoteControlMine(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetOptionPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetOption(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ToggleModulePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleToggleModule(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveMineFromMRATPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleRemoveMineFromMRAT(player, payload));
		});
	}

	private static void handleRemoteControlMine(ServerPlayer player, RemoteControlMinePayload payload) {
		ServerLevel level = player.level();
		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(payload.pos());

		if (!player.isSpectator() && state.getBlock() instanceof net.geforcemods.securitycraft.api.IExplosive explosive && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.IOwnable ownable && ownable.isOwnedBy(player))
			payload.action().act(explosive, level, payload.pos());
	}

	private static void handleToggleModule(ServerPlayer player, ToggleModulePayload payload) {
		ServerLevel level = player.level();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.IModuleInventory moduleInv))
			return;

		if (moduleInv instanceof net.geforcemods.securitycraft.api.IOwnable ownable && !ownable.isOwnedBy(player))
			return;

		if (moduleInv.isModuleEnabled(payload.moduleType())) {
			moduleInv.removeModule(payload.moduleType(), true);

			if (moduleInv instanceof net.geforcemods.securitycraft.api.LinkableBlockEntity linkable)
				linkable.propagate(new net.geforcemods.securitycraft.api.ILinkedAction.ModuleRemoved(payload.moduleType(), true), linkable);
		}
		else {
			net.minecraft.world.item.ItemStack stack = moduleInv.getModule(payload.moduleType());

			moduleInv.insertModule(stack, true);

			if (moduleInv instanceof net.geforcemods.securitycraft.api.LinkableBlockEntity linkable)
				linkable.propagate(new net.geforcemods.securitycraft.api.ILinkedAction.ModuleInserted(stack, (net.geforcemods.securitycraft.items.ModuleItem) stack.getItem(), true), linkable);
		}

		if (moduleInv instanceof net.minecraft.world.level.block.entity.BlockEntity be)
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
	}

	private static void handleSetOption(ServerPlayer player, SetOptionPayload payload) {
		ServerLevel level = player.level();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.ICustomizable customizable))
			return;

		if (customizable instanceof net.geforcemods.securitycraft.api.IOwnable ownable && !ownable.isOwnedBy(player))
			return;

		net.geforcemods.securitycraft.api.Option<?>[] options = customizable.customOptions();

		if (payload.optionIndex() < 0 || payload.optionIndex() >= options.length)
			return;

		net.geforcemods.securitycraft.api.Option<?> option = options[payload.optionIndex()];

		if (payload.toggle())
			option.toggle();
		else if (option instanceof net.geforcemods.securitycraft.api.Option.IntOption intOption)
			intOption.setValue((int) Math.round(payload.value()));
		else if (option instanceof net.geforcemods.securitycraft.api.Option.DoubleOption doubleOption)
			doubleOption.setValue(payload.value());

		customizable.onOptionChanged(option);

		if (customizable instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
			be.setChanged();
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
		}
	}

	private static void handleRemoveMineFromMRAT(ServerPlayer player, RemoveMineFromMRATPayload payload) {
		net.minecraft.world.item.ItemStack stack = net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, net.geforcemods.securitycraft.SCContent.MINE_REMOTE_ACCESS_TOOL);

		if (!player.isSpectator() && !stack.isEmpty()) {
			net.geforcemods.securitycraft.components.BoundMines mines = stack.get(net.geforcemods.securitycraft.SCContent.BOUND_MINES);

			if (mines != null)
				stack.set(net.geforcemods.securitycraft.SCContent.BOUND_MINES, mines.withoutSlot(payload.mineIndex()));
		}
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
