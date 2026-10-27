package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Registers SecurityCraft's payload types and the server-side receivers. */
public final class NetworkHandler {
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
		PayloadTypeRegistry.playC2S().register(SetListModuleDataPayload.TYPE, SetListModuleDataPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RemoteControlMinePayload.TYPE, RemoteControlMinePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RemoveMineFromMRATPayload.TYPE, RemoveMineFromMRATPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetOptionPayload.TYPE, SetOptionPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleModulePayload.TYPE, ToggleModulePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncKeycardSettingsPayload.TYPE, SyncKeycardSettingsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetKeycardUsesPayload.TYPE, SetKeycardUsesPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenAlarmScreenPayload.TYPE, OpenAlarmScreenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncAlarmSettingsPayload.TYPE, SyncAlarmSettingsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ToggleAlarmSoundPayload.TYPE, ToggleAlarmSoundPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenUsernameLoggerScreenPayload.TYPE, OpenUsernameLoggerScreenPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateLoggerPayload.TYPE, UpdateLoggerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ClearLoggerServerPayload.TYPE, ClearLoggerServerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ClearChangeDetectorServerPayload.TYPE, ClearChangeDetectorServerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncBlockChangeDetectorPayload.TYPE, SyncBlockChangeDetectorPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MountCameraPayload.TYPE, MountCameraPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DismountCameraPayload.TYPE, DismountCameraPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RemoveCameraTagPayload.TYPE, RemoveCameraTagPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetCameraPoweredPayload.TYPE, SetCameraPoweredPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetDefaultCameraViewingDirectionPayload.TYPE, SetDefaultCameraViewingDirectionPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleNightVisionPayload.TYPE, ToggleNightVisionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SetCameraViewPayload.TYPE, SetCameraViewPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncProjectorPayload.TYPE, SyncProjectorPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestSecureTradingStationTransactionsPayload.TYPE, RequestSecureTradingStationTransactionsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetStateOnDisguiseModulePayload.TYPE, SetStateOnDisguiseModulePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(RefreshDisguisableModelPayload.TYPE, RefreshDisguisableModelPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SyncLockedContainersPayload.TYPE, SyncLockedContainersPayload.CODEC);
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
		ServerPlayNetworking.registerGlobalReceiver(RemoveMineFromMRATPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleRemoveMineFromMRAT(player, payload));
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
		ServerPlayNetworking.registerGlobalReceiver(SyncKeycardSettingsPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncKeycardSettings(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetKeycardUsesPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetKeycardUses(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ClearChangeDetectorServerPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleClearChangeDetector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ClearLoggerServerPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleClearLogger(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(MountCameraPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleMountCamera(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(DismountCameraPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleDismountCamera(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveCameraTagPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleRemoveCameraTag(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetCameraPoweredPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetCameraPowered(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetDefaultCameraViewingDirectionPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSetDefaultCameraViewingDirection(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ToggleNightVisionPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleToggleNightVision(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncAlarmSettingsPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncAlarmSettings(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncBlockChangeDetectorPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncBlockChangeDetector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncProjectorPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleSyncProjector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestSecureTradingStationTransactionsPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> handleRequestSecureTradingStationTransactions(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetStateOnDisguiseModulePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.level().getServer();

			if (server != null)
				server.execute(() -> payload.handle(player));
		});
	}

	private static void handleSyncKeycardSettings(ServerPlayer player, SyncKeycardSettingsPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity be) || !be.isOwnedBy(player))
			return;

		be.setAcceptedLevels(payload.acceptedLevelsArray());
		be.setSignature(payload.signature());

		if (payload.link() && player.containerMenu instanceof net.geforcemods.securitycraft.inventory.KeycardReaderMenu openMenu && openMenu.be == be)
			openMenu.link(payload.usableBy());
	}

	private static void handleSetKeycardUses(ServerPlayer player, SetKeycardUsesPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity be) || !be.isOwnedBy(player) || !(player.containerMenu instanceof net.geforcemods.securitycraft.inventory.KeycardReaderMenu menu) || menu.be != be)
			return;

		menu.setKeycardUsesLeft(Math.max(0, payload.usesLeft()));
	}

	private static void handleRemoteControlMine(ServerPlayer player, RemoteControlMinePayload payload) {
		ServerLevel level = (ServerLevel) player.level();
		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(payload.pos());

		if (!player.isSpectator() && state.getBlock() instanceof net.geforcemods.securitycraft.api.IExplosive explosive && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.IOwnable ownable && ownable.isOwnedBy(player))
			payload.action().act(explosive, level, payload.pos());
	}

	private static void handleToggleModule(ServerPlayer player, ToggleModulePayload payload) {
		ServerLevel level = (ServerLevel) player.level();

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

		if (moduleInv instanceof BlockEntity be)
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
	}

	private static void handleSetOption(ServerPlayer player, SetOptionPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

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

		if (customizable instanceof BlockEntity be) {
			be.setChanged();
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
		}
	}

	private static void handleRemoveMineFromMRAT(ServerPlayer player, RemoveMineFromMRATPayload payload) {
		net.minecraft.world.item.ItemStack stack = net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, net.geforcemods.securitycraft.SCContent.MINE_REMOTE_ACCESS_TOOL);

		if (!player.isSpectator() && !stack.isEmpty()) {
			net.geforcemods.securitycraft.components.GlobalPositions mines = stack.get(net.geforcemods.securitycraft.SCContent.BOUND_MINES);

			if (mines != null)
				mines.remove(net.geforcemods.securitycraft.SCContent.BOUND_MINES, stack, payload.globalPos());
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
		ServerLevel level = (ServerLevel) player.level();

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

		net.geforcemods.securitycraft.api.PasscodeProtected keypad = resolvePasscodeProtected(player, payload.pos());

		if (keypad != null && keypad.getOwner().isOwner(player)) {
			keypad.setPasscode(payload.passcode());
			player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.set"), true);
		}
	}

	private static void handleCheckPasscode(ServerPlayer player, CheckPasscodePayload payload) {
		if (!validPasscode(payload.passcode()) || !inReach(player, payload.pos()))
			return;

		net.geforcemods.securitycraft.api.PasscodeProtected keypad = resolvePasscodeProtected(player, payload.pos());

		if (keypad != null && keypad.hasPasscode() && !keypad.isOnCooldown()) {
			if (keypad.checkPasscode(payload.passcode())) {
				player.closeContainer();
				keypad.activate((ServerLevel) player.level());
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.correct"), true);
			}
			else {
				keypad.onIncorrectPasscodeEntered(player);
				player.displayClientMessage(Component.translatable("messages.securitycraft:passcode.incorrect"), true);
			}
		}
	}

	/** A real {@link net.geforcemods.securitycraft.api.PasscodeProtected} block entity, or a generic {@link net.geforcemods.securitycraft.misc.ContainerLockData} lock at that position - see {@link net.geforcemods.securitycraft.misc.ContainerLockEnforcement}. */
	private static net.geforcemods.securitycraft.api.PasscodeProtected resolvePasscodeProtected(ServerPlayer player, BlockPos pos) {
		if (player.level().getBlockEntity(pos) instanceof net.geforcemods.securitycraft.api.PasscodeProtected keypad)
			return keypad;

		if (player.level() instanceof ServerLevel serverLevel)
			return net.geforcemods.securitycraft.misc.ContainerLockData.get(serverLevel).get(pos);

		return null;
	}

	public static void openAlarmScreen(ServerPlayer player, BlockPos pos) {
		ServerLevel level = (ServerLevel) player.level();

		if (level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be) {
			ServerPlayNetworking.send(player, new OpenAlarmScreenPayload(pos));
		}
	}

	public static void openUsernameLoggerScreen(ServerPlayer player, BlockPos pos) {
		ServerLevel level = (ServerLevel) player.level();

		if (level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be) {
			ServerPlayNetworking.send(player, new OpenUsernameLoggerScreenPayload(pos));
		}
	}

	private static void handleClearChangeDetector(ServerPlayer player, ClearChangeDetectorServerPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity be && be.isOwnedBy(player)) {
			BlockState state = be.getBlockState();

			be.getEntries().clear();
			be.setChanged();
			level.sendBlockUpdated(payload.pos(), state, state, 2);

			if (state.getValue(net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock.POWERED)) {
				level.setBlockAndUpdate(payload.pos(), state.setValue(net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock.POWERED, false));
				net.geforcemods.securitycraft.util.BlockUtils.updateIndirectNeighbors(level, payload.pos(), SCContent.BLOCK_CHANGE_DETECTOR, net.geforcemods.securitycraft.blocks.AbstractPanelBlock.getConnectedDirection(state).getOpposite());
			}
		}
	}

	private static void handleClearLogger(ServerPlayer player, ClearLoggerServerPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be && be.isOwnedBy(player)) {
			be.clearEntries();
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 2);
		}
	}

	private static void handleSyncAlarmSettings(ServerPlayer player, SyncAlarmSettingsPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be && be.isOwnedBy(player)) {
			be.setSound(payload.soundEvent());
			be.setPitch(payload.pitch());
			be.setSoundLength(payload.soundLength());
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 2);
		}
	}

	private static void handleSyncBlockChangeDetector(ServerPlayer player, SyncBlockChangeDetectorPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity be && be.isOwnedBy(player)) {
			be.setMode(payload.mode());
			be.setColor(payload.color());
			be.showHighlights(payload.showHighlights());
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 2);
		}
	}

	private static void handleSyncProjector(ServerPlayer player, SyncProjectorPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && level.isLoaded(payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity be && be.isOwnedBy(player)) {
			BlockState state = level.getBlockState(payload.pos());

			switch (payload.dataType()) {
				case WIDTH -> be.setProjectionWidth(payload.data());
				case HEIGHT -> be.setProjectionHeight(payload.data());
				case RANGE -> be.setProjectionRange(payload.data());
				case OFFSET -> be.setProjectionOffset(payload.data());
				case HORIZONTAL -> be.setHorizontal(payload.data() == 1);
				case OVERRIDING_BLOCKS -> be.setOverridingBlocks(payload.data() == 1);
				case BLOCK_STATE -> be.setProjectedState(net.minecraft.world.level.block.Block.stateById(payload.data()));
				case INVALID -> throw new UnsupportedOperationException("Invalid sync projector payload received!");
			}

			level.sendBlockUpdated(payload.pos(), state, state, 2);
		}
	}

	private static void handleRequestSecureTradingStationTransactions(ServerPlayer player, RequestSecureTradingStationTransactionsPayload payload) {
		ServerLevel level = (ServerLevel) player.level();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.SecureTradingStationBlockEntity be)
			be.doTransaction(player, payload.requestedTransactions());
	}

	private static void handleMountCamera(ServerPlayer player, MountCameraPayload payload) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos pos = payload.pos();
		BlockState state = level.getBlockState(pos);

		if (!player.isSpectator()) {
			if (level.isLoaded(pos) && state.getBlock() == SCContent.SECURITY_CAMERA && level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be && !be.isDisabled() && !be.isShutDown()) {
				if (be.isOwnedBy(player) || be.isAllowed(player))
					((net.geforcemods.securitycraft.blocks.SecurityCameraBlock) state.getBlock()).mountCamera(level, pos, player);
				else
					net.geforcemods.securitycraft.util.PlayerUtils.sendMessageToPlayer(player, net.geforcemods.securitycraft.util.Utils.localize(SCContent.CAMERA_MONITOR.getDescriptionId()), net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:notOwned", be.getOwner().getName()), net.minecraft.ChatFormatting.RED);
			}
			else
				net.geforcemods.securitycraft.util.PlayerUtils.sendMessageToPlayer(player, net.geforcemods.securitycraft.util.Utils.localize(SCContent.CAMERA_MONITOR.getDescriptionId()), net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:cameraMonitor.cameraNotAvailable", pos), net.minecraft.ChatFormatting.RED);
		}
	}

	private static void handleDismountCamera(ServerPlayer player) {
		if (player.getCamera() instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera cam)
			cam.stopViewing(player);
		else {
			player.camera = player;
			ServerPlayNetworking.send(player, new SetCameraViewPayload(player.getId()));
		}
	}

	private static void handleRemoveCameraTag(ServerPlayer player, RemoveCameraTagPayload payload) {
		ItemStack monitor = net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(player, SCContent.CAMERA_MONITOR);

		if (!player.isSpectator() && !monitor.isEmpty()) {
			net.geforcemods.securitycraft.components.NamedPositions cameras = monitor.get(SCContent.BOUND_CAMERAS);

			if (cameras != null)
				cameras.remove(() -> SCContent.BOUND_CAMERAS, monitor, payload.globalPos());
		}
	}

	private static void handleSetCameraPowered(ServerPlayer player, SetCameraPoweredPayload payload) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos pos = payload.pos();
		BlockEntity be = level.getBlockEntity(pos);

		if (!player.isSpectator() && ((be instanceof net.geforcemods.securitycraft.api.IOwnable ownable && ownable.isOwnedBy(player)) || (be instanceof net.geforcemods.securitycraft.api.IModuleInventory moduleInv && moduleInv.isAllowed(player)))) {
			BlockState state = level.getBlockState(pos);

			level.setBlockAndUpdate(pos, state.setValue(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.POWERED, payload.powered()));
			level.updateNeighborsAt(pos.relative(state.getValue(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.FACING), -1), state.getBlock());
		}
	}

	private static void handleSetDefaultCameraViewingDirection(ServerPlayer player, SetDefaultCameraViewingDirectionPayload payload) {
		if (!player.isSpectator() && player.getCamera() instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera camera && camera.getId() == payload.id() && camera.level().getBlockEntity(camera.blockPosition()) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be) {
			if (!be.isOwnedBy(player)) {
				player.displayClientMessage(net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:security_camera.no_permission"), true);
				return;
			}

			if (be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.SMART)) {
				be.setDefaultViewingDirection(payload.xRot(), payload.yRot(), payload.zoom());
				player.displayClientMessage(net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:security_camera.direction_set"), true);
			}
			else
				player.displayClientMessage(net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:security_camera.smart_module_needed"), true);
		}
	}

	private static void handleToggleNightVision(ServerPlayer player) {
		if (net.geforcemods.securitycraft.ConfigHandler.allowCameraNightVision && net.geforcemods.securitycraft.util.PlayerUtils.isPlayerMountedOnCamera(player)) {
			if (player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) {
				if (player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) instanceof net.geforcemods.securitycraft.entity.camera.CameraNightVisionEffectInstance)
					player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
			}
			else
				player.addEffect(new net.geforcemods.securitycraft.entity.camera.CameraNightVisionEffectInstance());
		}
	}

	private static boolean validPasscode(String passcode) {
		return passcode != null && !passcode.isBlank();
	}

	private static boolean inReach(ServerPlayer player, BlockPos pos) {
		return pos.closerThan(player.blockPosition(), REACH);
	}
}
