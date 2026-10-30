package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
		ServerPlayNetworking.registerGlobalReceiver(RemoteControlMinePayload.CHANNEL, (server, player, handler, buf, sender) -> {
			RemoteControlMinePayload payload = RemoteControlMinePayload.read(buf);

			server.execute(() -> handleRemoteControlMine(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetOptionPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetOptionPayload payload = SetOptionPayload.read(buf);

			server.execute(() -> handleSetOption(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ToggleModulePayload.CHANNEL, (server, player, handler, buf, sender) -> {
			ToggleModulePayload payload = ToggleModulePayload.read(buf);

			server.execute(() -> handleToggleModule(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveMineFromMRATPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			RemoveMineFromMRATPayload payload = RemoveMineFromMRATPayload.read(buf);

			server.execute(() -> handleRemoveMineFromMRAT(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncKeycardSettingsPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncKeycardSettingsPayload payload = SyncKeycardSettingsPayload.read(buf);

			server.execute(() -> handleSyncKeycardSettings(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetKeycardUsesPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetKeycardUsesPayload payload = SetKeycardUsesPayload.read(buf);

			server.execute(() -> handleSetKeycardUses(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetStateOnDisguiseModulePayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetStateOnDisguiseModulePayload payload = SetStateOnDisguiseModulePayload.read(buf);

			server.execute(() -> payload.handle(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncAlarmSettingsPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncAlarmSettingsPayload payload = SyncAlarmSettingsPayload.read(buf);

			server.execute(() -> handleSyncAlarmSettings(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ClearLoggerServerPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			ClearLoggerServerPayload payload = ClearLoggerServerPayload.read(buf);

			server.execute(() -> handleClearLogger(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ClearChangeDetectorServerPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			ClearChangeDetectorServerPayload payload = ClearChangeDetectorServerPayload.read(buf);

			server.execute(() -> handleClearChangeDetector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncBlockChangeDetectorPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncBlockChangeDetectorPayload payload = SyncBlockChangeDetectorPayload.read(buf);

			server.execute(() -> handleSyncBlockChangeDetector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncProjectorPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SyncProjectorPayload payload = SyncProjectorPayload.read(buf);

			server.execute(() -> handleSyncProjector(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestSecureTradingStationTransactionsPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			RequestSecureTradingStationTransactionsPayload payload = RequestSecureTradingStationTransactionsPayload.read(buf);

			server.execute(() -> handleRequestSecureTradingStationTransactions(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(MountCameraPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			MountCameraPayload payload = MountCameraPayload.read(buf);

			server.execute(() -> handleMountCamera(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(DismountCameraPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			server.execute(() -> handleDismountCamera(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveCameraTagPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			RemoveCameraTagPayload payload = RemoveCameraTagPayload.read(buf);

			server.execute(() -> handleRemoveCameraTag(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetCameraPoweredPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetCameraPoweredPayload payload = SetCameraPoweredPayload.read(buf);

			server.execute(() -> handleSetCameraPowered(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(SetDefaultCameraViewingDirectionPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetDefaultCameraViewingDirectionPayload payload = SetDefaultCameraViewingDirectionPayload.read(buf);

			server.execute(() -> handleSetDefaultCameraViewingDirection(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ToggleNightVisionPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			server.execute(() -> handleToggleNightVision(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(SyncTrophySystemPayload.TYPE, (payload, player, responseSender) -> handleSyncTrophySystem(player, payload));
		ServerPlayNetworking.registerGlobalReceiver(SyncRiftStabilizerPayload.TYPE, (payload, player, responseSender) -> handleSyncRiftStabilizer(player, payload));
		ServerPlayNetworking.registerGlobalReceiver(SetSentryModePayload.TYPE, (payload, player, responseSender) -> handleSetSentryMode(player, payload));
		ServerPlayNetworking.registerGlobalReceiver(RemoveSentryFromSRATPayload.TYPE, (payload, player, responseSender) -> handleRemoveSentryFromSRAT(player, payload));
		ServerPlayNetworking.registerGlobalReceiver(SetGhostSlotPayload.CHANNEL, (server, player, handler, buf, sender) -> {
			SetGhostSlotPayload payload = SetGhostSlotPayload.read(buf);

			server.execute(() -> payload.handle(player));
		});
	}

	private static void handleSyncKeycardSettings(ServerPlayer player, SyncKeycardSettingsPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity be) || !be.isOwnedBy(player))
			return;

		be.setAcceptedLevels(payload.acceptedLevels());
		be.setSignature(payload.signature());

		if (payload.link() && player.containerMenu instanceof net.geforcemods.securitycraft.inventory.KeycardReaderMenu openMenu && openMenu.be == be)
			openMenu.link(payload.usableBy());
	}

	private static void handleSetKeycardUses(ServerPlayer player, SetKeycardUsesPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity be) || !be.isOwnedBy(player) || !(player.containerMenu instanceof net.geforcemods.securitycraft.inventory.KeycardReaderMenu menu) || menu.be != be)
			return;

		menu.setKeycardUsesLeft(Math.max(0, payload.usesLeft()));
	}

	private static void handleRemoteControlMine(ServerPlayer player, RemoteControlMinePayload payload) {
		ServerLevel level = player.serverLevel();
		BlockState state = level.getBlockState(payload.pos());

		if (!player.isSpectator() && state.getBlock() instanceof net.geforcemods.securitycraft.api.IExplosive explosive && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.api.IOwnable ownable && ownable.isOwnedBy(player))
			payload.action().act(explosive, level, payload.pos());
	}

	private static void handleToggleModule(ServerPlayer player, ToggleModulePayload payload) {
		ServerLevel level = player.serverLevel();

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
			ItemStack stack = moduleInv.getModule(payload.moduleType());

			moduleInv.insertModule(stack, true);

			if (moduleInv instanceof net.geforcemods.securitycraft.api.LinkableBlockEntity linkable)
				linkable.propagate(new net.geforcemods.securitycraft.api.ILinkedAction.ModuleInserted(stack, (net.geforcemods.securitycraft.items.ModuleItem) stack.getItem(), true), linkable);
		}

		if (moduleInv instanceof BlockEntity be)
			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
	}

	private static void handleSetOption(ServerPlayer player, SetOptionPayload payload) {
		ServerLevel level = player.serverLevel();

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
		ItemStack stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.MINE_REMOTE_ACCESS_TOOL);

		if (!player.isSpectator() && !stack.isEmpty()) {
			net.minecraft.nbt.CompoundTag tag = stack.getOrCreateTag();

			if (tag.contains("mine" + payload.mineIndex()))
				tag.remove("mine" + payload.mineIndex());
		}
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

	private static void handleSyncAlarmSettings(ServerPlayer player, SyncAlarmSettingsPayload payload) {
		ServerLevel level = player.serverLevel();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be && be.isOwnedBy(player)) {
			if (!payload.soundEvent().equals(be.getSound().getLocation()))
				be.setSound(payload.soundEvent());

			if (payload.pitch() != be.getPitch())
				be.setPitch(payload.pitch());

			if (payload.soundLength() != be.getSoundLength())
				be.setSoundLength(payload.soundLength());

			level.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
		}
	}

	public static void openAlarmScreen(ServerPlayer player, BlockPos pos) {
		ServerPlayNetworking.send(player, OpenAlarmScreenPayload.CHANNEL, new OpenAlarmScreenPayload(pos).write());
	}

	public static void openUsernameLoggerScreen(ServerPlayer player, BlockPos pos) {
		if (player.level().getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)
			be.syncLoggedPlayersToClient(player);
		ServerPlayNetworking.send(player, OpenUsernameLoggerScreenPayload.CHANNEL, new OpenUsernameLoggerScreenPayload(pos).write());
	}

	private static void handleClearLogger(ServerPlayer player, ClearLoggerServerPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be) || !be.isOwnedBy(player))
			return;

		be.clearEntries();
		be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
	}

	private static void handleClearChangeDetector(ServerPlayer player, ClearChangeDetectorServerPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity be) || !be.isOwnedBy(player))
			return;

		BlockState state = be.getBlockState();

		be.getEntries().clear();
		be.setChanged();
		level.sendBlockUpdated(payload.pos(), state, state, 2);

		if (state.getValue(net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock.POWERED)) {
			level.setBlockAndUpdate(payload.pos(), state.setValue(net.geforcemods.securitycraft.blocks.BlockChangeDetectorBlock.POWERED, false));
			net.geforcemods.securitycraft.util.BlockUtils.updateIndirectNeighbors(level, payload.pos(), SCContent.BLOCK_CHANGE_DETECTOR, net.geforcemods.securitycraft.blocks.AbstractPanelBlock.getConnectedDirection(state).getOpposite());
		}
	}

	private static void handleSyncBlockChangeDetector(ServerPlayer player, SyncBlockChangeDetectorPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity be) || !be.isOwnedBy(player))
			return;

		BlockState state = level.getBlockState(payload.pos());

		be.setMode(payload.mode());
		be.showHighlights(payload.showHighlights());
		be.setColor(payload.color());
		be.setChanged();
		level.sendBlockUpdated(payload.pos(), state, state, 2);
	}

	private static void handleSyncProjector(ServerPlayer player, SyncProjectorPayload payload) {
		ServerLevel level = player.serverLevel();

		if (player.isSpectator() || !inReach(player, payload.pos()) || !(level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity be) || !be.isOwnedBy(player))
			return;

		BlockState state = level.getBlockState(payload.pos());

		switch (payload.dataType()) {
			case WIDTH -> be.setProjectionWidth(payload.data());
			case HEIGHT -> be.setProjectionHeight(payload.data());
			case RANGE -> be.setProjectionRange(payload.data());
			case OFFSET -> be.setProjectionOffset(payload.data());
			case HORIZONTAL -> be.setHorizontal(payload.data() == 1);
			case OVERRIDING_BLOCKS -> be.setOverridingBlocks(payload.data() == 1);
			case BLOCK_STATE -> be.setProjectedState(net.minecraft.world.level.block.Block.stateById(payload.data()));
			case INVALID -> {}
		}

		level.sendBlockUpdated(payload.pos(), state, state, 2);
	}

	public static void openKeypadScreen(ServerPlayer player, BlockPos pos, boolean setup, String ownerName) {
		ServerPlayNetworking.send(player, OpenKeypadScreenPayload.CHANNEL, new OpenKeypadScreenPayload(pos, setup, ownerName).write());
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

	private static boolean validPasscode(String passcode) {
		return passcode != null && !passcode.isBlank();
	}

	private static void handleRequestSecureTradingStationTransactions(ServerPlayer player, RequestSecureTradingStationTransactionsPayload payload) {
		ServerLevel level = player.serverLevel();

		if (!player.isSpectator() && inReach(player, payload.pos()) && level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.SecureTradingStationBlockEntity be)
			be.doTransaction(player, payload.requestedTransactions());
	}

	private static void handleMountCamera(ServerPlayer player, MountCameraPayload payload) {
		Level level = player.level();
		BlockPos pos = payload.pos();
		BlockState state = level.getBlockState(pos);

		if (!player.isSpectator()) {
			if (level.isLoaded(pos) && state.getBlock() == SCContent.SECURITY_CAMERA && level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be && !be.isDisabled() && !be.isShutDown()) {
				if (be.isOwnedBy(player) || be.isAllowed(player))
					((net.geforcemods.securitycraft.blocks.SecurityCameraBlock) state.getBlock()).mountCamera(level, pos, player);
				else
					PlayerUtils.sendMessageToPlayer(player, net.geforcemods.securitycraft.util.Utils.localize(SCContent.CAMERA_MONITOR.getDescriptionId()), net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:notOwned", be.getOwner().getName()), ChatFormatting.RED);
			}
			else
				PlayerUtils.sendMessageToPlayer(player, net.geforcemods.securitycraft.util.Utils.localize(SCContent.CAMERA_MONITOR.getDescriptionId()), net.geforcemods.securitycraft.util.Utils.localize("messages.securitycraft:cameraMonitor.cameraNotAvailable", pos), ChatFormatting.RED);
		}
	}

	private static void handleDismountCamera(ServerPlayer player) {
		if (player.getCamera() instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera cam)
			cam.stopViewing(player);
	}

	private static void handleRemoveCameraTag(ServerPlayer player, RemoveCameraTagPayload payload) {
		ItemStack monitor = PlayerUtils.getItemStackFromAnyHand(player, SCContent.CAMERA_MONITOR);

		if (!player.isSpectator() && !monitor.isEmpty() && monitor.hasTag())
			monitor.getTag().remove(net.geforcemods.securitycraft.items.CameraMonitorItem.getTagNameFromPosition(monitor.getTag(), net.geforcemods.securitycraft.items.CameraMonitorItem.getCameraPositions(monitor.getTag()).get(payload.camId() - 1).getLeft()));
	}

	private static void handleSetCameraPowered(ServerPlayer player, SetCameraPoweredPayload payload) {
		Level level = player.level();
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
		if (net.geforcemods.securitycraft.ConfigHandler.allowCameraNightVision && PlayerUtils.isPlayerMountedOnCamera(player)) {
			if (player.hasEffect(MobEffects.NIGHT_VISION)) {
				if (player.getEffect(MobEffects.NIGHT_VISION) instanceof net.geforcemods.securitycraft.entity.camera.CameraNightVisionEffectInstance)
					player.removeEffect(MobEffects.NIGHT_VISION);
			}
			else
				player.addEffect(new net.geforcemods.securitycraft.entity.camera.CameraNightVisionEffectInstance());
		}
	}

	private static boolean inReach(ServerPlayer player, BlockPos pos) {
		return pos.closerThan(player.blockPosition(), REACH);
	}

	public static void sendToServer(SCPayload payload) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
	}

	public static void sendToTracking(ServerLevel level, BlockPos pos, SCPayload payload) {
		for (ServerPlayer player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(level, pos)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void openRiftStabilizerScreen(ServerPlayer player, BlockPos pos) {
		ServerPlayNetworking.send(player, OpenRiftStabilizerScreenPayload.CHANNEL, new OpenRiftStabilizerScreenPayload(pos).write());
	}

	public static void openSRATScreen(ServerPlayer player, net.minecraft.nbt.CompoundTag tag) {
		ServerPlayNetworking.send(player, OpenSRATScreenPayload.CHANNEL, new OpenSRATScreenPayload(tag).write());
	}

	private static void handleSyncTrophySystem(ServerPlayer player, SyncTrophySystemPayload payload) {
		Level level = player.level();
		BlockPos pos = payload.pos();

		if (!player.isSpectator() && payload.projectileType() != net.minecraft.world.entity.EntityType.PLAYER && level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.TrophySystemBlockEntity be && be.isOwnedBy(player)) {
			BlockState state = level.getBlockState(pos);

			be.setFilter(payload.projectileType(), payload.allowed());
			level.sendBlockUpdated(pos, state, state, 2);
		}
	}

	private static void handleSyncRiftStabilizer(ServerPlayer player, SyncRiftStabilizerPayload payload) {
		if (!player.isSpectator() && payload.teleportationType() != null) {
			Level level = player.level();
			BlockPos pos = payload.pos();

			if (level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity be && be.isOwnedBy(player)) {
				BlockState state = level.getBlockState(pos);

				be.setFilter(payload.teleportationType(), payload.allowed());
				level.sendBlockUpdated(pos, state, state, 2);

				net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity connectedBlockEntity = net.geforcemods.securitycraft.blocks.RiftStabilizerBlock.getConnectedBlockEntity(level, pos);

				if (connectedBlockEntity != null)
					level.sendBlockUpdated(connectedBlockEntity.getBlockPos(), connectedBlockEntity.getBlockState(), connectedBlockEntity.getBlockState(), 2);
			}
		}
	}

	private static void handleSetSentryMode(ServerPlayer player, SetSentryModePayload payload) {
		if (!player.isSpectator()) {
			Level level = player.level();

			for (SetSentryModePayload.Info info : payload.sentries()) {
				if (level.isLoaded(info.pos())) {
					java.util.List<net.geforcemods.securitycraft.entity.sentry.Sentry> sentries = level.<net.geforcemods.securitycraft.entity.sentry.Sentry>getEntitiesOfClass(net.geforcemods.securitycraft.entity.sentry.Sentry.class, new net.minecraft.world.phys.AABB(info.pos()));

					if (!sentries.isEmpty() && sentries.get(0).isOwnedBy(player))
						sentries.get(0).toggleMode(player, info.mode(), false);
				}
			}
		}
	}

	private static void handleRemoveSentryFromSRAT(ServerPlayer player, RemoveSentryFromSRATPayload payload) {
		if (!player.isSpectator()) {
			ItemStack stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.REMOTE_ACCESS_SENTRY);

			if (!stack.isEmpty() && stack.hasTag()) {
				stack.getTag().remove("sentry" + payload.sentryIndex());
				stack.getTag().remove("sentry" + payload.sentryIndex() + "_name");
			}
		}
	}
}
