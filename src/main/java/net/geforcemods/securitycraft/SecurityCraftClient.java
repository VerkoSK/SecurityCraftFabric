package net.geforcemods.securitycraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.geforcemods.securitycraft.misc.TintMode;
import net.geforcemods.securitycraft.network.OpenKeypadScreenPayload;
import net.geforcemods.securitycraft.screen.CheckPasscodeScreen;
import net.geforcemods.securitycraft.screen.SetPasscodeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;

/** Client entrypoint: keypad screen packet + configurable reinforced-block tint (see {@link TintMode}). */
public class SecurityCraftClient implements ClientModInitializer {
	/** Opens the allow/deny-list editor for the given module stack (called client-side only). */
	public static void openEditModuleScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.EditModuleScreen(stack));
	}

	/** Opens the SecurityCraft Manual (called client-side only). */
	public static void openManualScreen() {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.SCManualScreen());
	}

	/** Opens the mine remote access tool screen for the given stack (called client-side only). */
	public static void openMRATScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.MineRemoteAccessToolScreen(stack));
	}

	public static final net.minecraft.client.model.geom.ModelLayerLocation SECURITY_CAMERA_LOCATION = new net.minecraft.client.model.geom.ModelLayerLocation(SCContent.id("security_camera"), "main");
	public static final net.minecraft.resources.ResourceLocation CAMERA_DASHBOARD = SCContent.id("textures/gui/camera/camera_dashboard.png");
	public static final net.minecraft.resources.ResourceLocation NIGHT_VISION = new net.minecraft.resources.ResourceLocation("textures/mob_effect/night_vision.png");
	public static final float EMPTY_STATE = 0.0F, UNKNOWN_STATE = 0.25F, NOT_LINKED_STATE = 0.5F, LINKED_STATE = 0.75F;
	private static final net.minecraft.network.chat.Component REDSTONE_NOTE = net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.toggleRedstoneNote");
	private static final net.minecraft.network.chat.Component SMART_MODULE_NOTE = net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.smartModuleNote");
	private static final CameraKeyInfoEntry[] CAMERA_KEY_INFO_LIST = {
			new CameraKeyInfoEntry(() -> true, options -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.lookAround", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()), $ -> true),
			new CameraKeyInfoEntry(() -> true, options -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.exit", options.keyShift.getTranslatedKeyMessage()), $ -> true),
			new CameraKeyInfoEntry(() -> true, $ -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.zoom", net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomIn.getTranslatedKeyMessage(), net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomOut.getTranslatedKeyMessage()), $ -> true),
			new CameraKeyInfoEntry(() -> ConfigHandler.allowCameraNightVision, $ -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.activateNightVision", net.geforcemods.securitycraft.misc.KeyBindings.cameraActivateNightVision.getTranslatedKeyMessage()), $ -> true),
			new CameraKeyInfoEntry(() -> true, $ -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.toggleRedstone", net.geforcemods.securitycraft.misc.KeyBindings.cameraEmitRedstone.getTranslatedKeyMessage()), be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.REDSTONE)),
			new CameraKeyInfoEntry(() -> true, $ -> REDSTONE_NOTE, be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.REDSTONE)),
			new CameraKeyInfoEntry(() -> true, $ -> net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:camera.setDefaultViewingDirection", net.geforcemods.securitycraft.misc.KeyBindings.setDefaultViewingDirection.getTranslatedKeyMessage()), be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.SMART)),
			new CameraKeyInfoEntry(() -> true, $ -> SMART_MODULE_NOTE, be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.SMART))
	};
	private static float cameraInfoMessageTime;

	public static boolean isPlayerMountedOnCamera() {
		return Minecraft.getInstance().cameraEntity instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera;
	}

	public static void displayCameraMonitorScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.CameraMonitorScreen(stack));
	}

	public static void resetCameraInfoMessageTime() {
		cameraInfoMessageTime = 200;
	}

	public record CameraKeyInfoEntry(java.util.function.Supplier<Boolean> enabled, java.util.function.Function<net.minecraft.client.Options, net.minecraft.network.chat.Component> text, java.util.function.Predicate<net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity> whiteText) {
		public void drawString(net.minecraft.client.Options options, net.minecraft.client.gui.GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int scaledWidth, int scaledHeight, int heightOffset, net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be, int alpha) {
			net.minecraft.network.chat.Component text = text().apply(options);
			int textColor = whiteText().test(be) ? 0xFFFFFF : 0xFF3377;

			guiGraphics.drawString(font, text, scaledWidth - font.width(text) - 8, scaledHeight - heightOffset, textColor + (alpha << 24), true);
		}
	}

	public static float getLinkingState(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stackInHand, java.util.function.Predicate<net.minecraft.world.phys.BlockHitResult> isValidHitResult, int tagSize, java.util.function.BiFunction<net.minecraft.nbt.CompoundTag, Integer, Integer[]> getCoords) {
		Minecraft mc = Minecraft.getInstance();
		double reachDistance = mc.gameMode != null ? mc.gameMode.getPickRange() : 4.5D;
		double eyeHeight = player.getEyeHeight();
		net.minecraft.world.phys.Vec3 lookVec = new net.minecraft.world.phys.Vec3(player.getX() + player.getLookAngle().x * reachDistance, eyeHeight + player.getY() + player.getLookAngle().y * reachDistance, player.getZ() + player.getLookAngle().z * reachDistance);

		if (level != null) {
			net.minecraft.world.phys.BlockHitResult hitResult = level.clip(new net.minecraft.world.level.ClipContext(new net.minecraft.world.phys.Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ()), lookVec, net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player));

			if (hitResult != null && hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && isValidHitResult.test(hitResult)) {
				for (int i = 1; i <= tagSize; i++) {
					Integer[] coords = getCoords.apply(stackInHand.getOrCreateTag(), i);

					if (coords != null && coords.length == 3 && coords[0] == hitResult.getBlockPos().getX() && coords[1] == hitResult.getBlockPos().getY() && coords[2] == hitResult.getBlockPos().getZ())
						return LINKED_STATE;
				}

				return NOT_LINKED_STATE;
			}
		}

		return UNKNOWN_STATE;
	}

	public static void cameraOverlay(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		net.minecraft.world.level.Level level = mc.level;
		if (level == null || mc.cameraEntity == null)
			return;

		net.minecraft.core.BlockPos pos = mc.cameraEntity.blockPosition();
		com.mojang.blaze3d.platform.Window window = mc.getWindow();
		int scaledWidth = window.getGuiScaledWidth();
		int scaledHeight = window.getGuiScaledHeight();

		if (mc.options.renderDebug)
			return;

		if (!(level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be))
			return;

		net.minecraft.client.gui.Font font = mc.font;
		net.minecraft.client.Options options = mc.options;
		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
		long dayTime = level.getDayTime();
		int hours24 = (int) ((float) dayTime / 1000L + 6L) % 24;
		int hours = hours24 % 12;
		int minutes = (int) (dayTime / 16.666666F % 60.0F);
		String time = String.format("%02d:%02d %s", Integer.valueOf(hours < 1 ? 12 : hours), Integer.valueOf(minutes), hours24 < 12 ? "AM" : "PM");
		int timeY = 25;

		if (be.hasCustomName()) {
			net.minecraft.network.chat.Component cameraName = be.getCustomName();

			guiGraphics.drawString(font, cameraName, scaledWidth - font.width(cameraName) - 8, 25, 0xFFFFFF, true);
			timeY += 10;
		}

		guiGraphics.drawString(font, time, scaledWidth - font.width(time) - 4, timeY, 0xFFFFFF, true);

		if (cameraInfoMessageTime >= 0) {
			float fadeOutPartialTick = Math.max(cameraInfoMessageTime + 1.0F - partialTicks, 1.0F);
			int alpha = (int) Math.ceil(255.0F * Math.min(20.0F, fadeOutPartialTick) / 20.0F);
			int heightOffset = 10;

			for (int i = CAMERA_KEY_INFO_LIST.length - 1; i >= 0; i--) {
				CameraKeyInfoEntry entry = CAMERA_KEY_INFO_LIST[i];

				if (entry.enabled().get()) {
					entry.drawString(options, guiGraphics, font, scaledWidth, scaledHeight, heightOffset, be, alpha);
					heightOffset += 10;
				}
			}
		}

		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		guiGraphics.blit(CAMERA_DASHBOARD, 5, 0, 0, 0, 90, 20);
		guiGraphics.blit(CAMERA_DASHBOARD, window.getGuiScaledWidth() - 70, 5, 190, 0, 65, 30);

		if (mc.player != null && !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION))
			guiGraphics.blit(CAMERA_DASHBOARD, 28, 4, 90, 12, 16, 11);
		else
			guiGraphics.blit(NIGHT_VISION, 27, -1, 0, 0, 18, 18, 18, 18);

		if (state.hasProperty(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.FACING) && state.getSignal(level, pos, state.getValue(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.FACING)) == 0) {
			if (!be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.REDSTONE))
				net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.NOT_INSTALLED.render(guiGraphics, 12, 2);
			else
				net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.DEACTIVATED.render(guiGraphics, 12, 2);
		}
		else
			net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.ACTIVATED.render(guiGraphics, 12, 2);
	}

	private static void handleSetCameraView(Minecraft mc, net.geforcemods.securitycraft.network.SetCameraViewPayload payload) {
		if (mc.level == null)
			return;

		net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.entityId());
		boolean isMountingCamera = entity instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera;

		if (isMountingCamera || entity instanceof net.minecraft.world.entity.player.Player) {
			mc.setCameraEntity(entity);

			if (isMountingCamera) {
				net.geforcemods.securitycraft.entity.camera.CameraController.setCameraMountedTimestamp();
				net.geforcemods.securitycraft.entity.camera.CameraController.previousCameraType = mc.options.getCameraType();
				mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
				mc.gui.setOverlayMessage(net.geforcemods.securitycraft.util.Utils.localize("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage()), false);
				resetCameraInfoMessageTime();
			}
			else {
				if (net.geforcemods.securitycraft.entity.camera.CameraController.previousCameraType != null)
					mc.options.setCameraType(net.geforcemods.securitycraft.entity.camera.CameraController.previousCameraType);

				net.geforcemods.securitycraft.entity.camera.CameraController.resetOverlaysAfterDismount = true;
			}

			mc.levelRenderer.allChanged();
		}
	}

	/** Live tint colour for reinforced blocks: white base multiplied by the configured tint (grey by default). */
	private static int reinforcedTint() {
		return TintMode.tint(Minecraft.getInstance().player, 0xFFFFFFFF, null);
	}

	/** Colour of a laser beam: the dyed colour of the lens in the source laser for this beam's axis, or red. */
	private static int laserFieldColor(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockAndTintGetter view, net.minecraft.core.BlockPos pos, int tintIndex) {
		if (tintIndex != 0)
			return -1;

		if (view != null && pos != null) {
			net.minecraft.core.Direction axis = net.geforcemods.securitycraft.blocks.LaserFieldBlock.getFieldDirection(state);

			if (axis != null) {
				for (net.minecraft.core.Direction dir : new net.minecraft.core.Direction[] {
						axis, axis.getOpposite()
				}) {
					for (int i = 1; i <= ConfigHandler.laserBlockRange; i++) {
						net.minecraft.core.BlockPos offsetPos = pos.relative(dir, i);

						if (view.getBlockEntity(offsetPos) instanceof net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity laser) {
							net.minecraft.world.item.ItemStack lens = laser.getLensContainer().getItem(dir.getOpposite().ordinal());

							if (net.geforcemods.securitycraft.util.Utils.hasLensColor(lens))
								return 0xFF000000 | net.geforcemods.securitycraft.util.Utils.getLensColor(lens);

							return 0xFFFFFFFF;
						}

						net.minecraft.world.level.block.state.BlockState offsetState = view.getBlockState(offsetPos);

						if (offsetState.getBlock() != SCContent.LASER_FIELD && !offsetState.isAir())
							break;
					}
				}
			}
		}

		return 0xFFFFFFFF;
	}

	/**
	 * Vanilla's sign renderers are typed to the vanilla sign block entity, so a renderer extending one cannot also
	 * declare itself a renderer of the secret sign's own block entity. Upstream never has to say so, because Forge's
	 * registration is not generic; this is the cast that costs.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerSignRenderer(net.minecraft.world.level.block.entity.BlockEntityType<T> type, java.util.function.Function<net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context, ? extends net.minecraft.client.renderer.blockentity.BlockEntityRenderer<?>> provider) {
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(type, ctx -> (net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T>) provider.apply(ctx));
	}

	@Override
	public void onInitializeClient() {
		SCClientConfig.load();
		net.geforcemods.securitycraft.misc.UpdateChecker.run();
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
			java.util.function.Consumer<net.minecraft.network.chat.Component> messageSink = message -> {
				if (client.player != null)
					client.player.displayClientMessage(message, false);
			};

			net.geforcemods.securitycraft.misc.WelcomeMessage.show(messageSink);
			net.geforcemods.securitycraft.misc.UpdateChecker.notify(messageSink);
		}));
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			OpenKeypadScreenPayload payload = OpenKeypadScreenPayload.read(buf);

			client.execute(() -> {
				net.minecraft.network.chat.Component title = client.level != null ? client.level.getBlockState(payload.pos()).getBlock().getName() : net.minecraft.network.chat.Component.empty();

				client.setScreen(payload.setup() ? new SetPasscodeScreen(payload.pos(), title) : new CheckPasscodeScreen(payload.pos(), title));
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenAlarmScreenPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.OpenAlarmScreenPayload payload = net.geforcemods.securitycraft.network.OpenAlarmScreenPayload.read(buf);

			client.execute(() -> {
				if (client.level != null && client.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be)
					client.setScreen(new net.geforcemods.securitycraft.screen.AlarmScreen(be, be.getSound().getLocation()));
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.ToggleAlarmSoundPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.ToggleAlarmSoundPayload payload = net.geforcemods.securitycraft.network.ToggleAlarmSoundPayload.read(buf);

			client.execute(() -> {
				if (client.level != null && client.level.getBlockEntity(payload.bePos()) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be) {
					if (payload.shouldPlay())
						be.playSound(client.level, payload.getX(), payload.getY(), payload.getZ(), payload.volume(), payload.seed());
					else
						be.stopPlayingSound();
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenUsernameLoggerScreenPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.OpenUsernameLoggerScreenPayload payload = net.geforcemods.securitycraft.network.OpenUsernameLoggerScreenPayload.read(buf);

			client.execute(() -> {
				if (client.level != null && client.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)
					client.setScreen(new net.geforcemods.securitycraft.screen.UsernameLoggerScreen(be));
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLoggerPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.UpdateLoggerPayload payload = net.geforcemods.securitycraft.network.UpdateLoggerPayload.read(buf);

			client.execute(() -> {
				if (client.level != null && client.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)
					be.getEntries()[payload.index()] = new net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity.UsernameLoggerEntry(payload.username(), payload.uuid(), payload.timestamp());
			});
		});
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.BLOCK_REINFORCER_MENU, net.geforcemods.securitycraft.screen.BlockReinforcerScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.LASER_BLOCK_MENU, net.geforcemods.securitycraft.screen.LaserBlockScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.DISGUISE_MODULE_MENU, net.geforcemods.securitycraft.screen.DisguiseModuleScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.CUSTOMIZE_BLOCK_MENU, net.geforcemods.securitycraft.screen.CustomizeBlockScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.REINFORCED_LECTERN_MENU, net.geforcemods.securitycraft.screen.ReinforcedLecternScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.KEYPAD_FURNACE_MENU, net.geforcemods.securitycraft.screen.KeypadFurnaceScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.KEYPAD_SMOKER_MENU, net.geforcemods.securitycraft.screen.KeypadSmokerScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.KEYPAD_BLAST_FURNACE_MENU, net.geforcemods.securitycraft.screen.KeypadBlastFurnaceScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.SINGLE_LENS_MENU, net.geforcemods.securitycraft.screen.SingleLensScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.KEYCARD_READER_MENU, net.geforcemods.securitycraft.screen.KeycardReaderScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.KEYCARD_HOLDER_MENU, net.geforcemods.securitycraft.screen.KeycardHolderScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.BLOCK_CHANGE_DETECTOR_MENU, net.geforcemods.securitycraft.screen.BlockChangeDetectorScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.PROJECTOR_MENU, net.geforcemods.securitycraft.screen.ProjectorScreen::new);
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.SECURE_TRADING_STATION_MENU, net.geforcemods.securitycraft.screen.SecureTradingStationScreen::new);
		//Without these the claymore's lens slot draws nothing and, worse, spawning a bouncing betty or an IMS bomb
		//throws because Fabric has no renderer registered for their entity types.
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.BOUNCING_BETTY_ENTITY, net.geforcemods.securitycraft.renderers.BouncingBettyRenderer::new);
		//IMSBombRenderer bakes this layer in its constructor, so it has to be registered or the bake throws.
		net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(net.geforcemods.securitycraft.renderers.IMSBombRenderer.IMS_BOMB_LOCATION, net.geforcemods.securitycraft.models.IMSBombModel::createLayer);
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.IMS_BOMB_ENTITY, net.geforcemods.securitycraft.renderers.IMSBombRenderer::new);
		//the chest's block model is empty, so both the placed block and the item are drawn by its own renderer
		//the secret signs draw their text only for the players allowed to read it
		registerSignRenderer(SCContent.SECRET_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretSignRenderer::new);
		registerSignRenderer(SCContent.SECRET_HANGING_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretHangingSignRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYPAD_CHEST_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.KeypadChestRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(SCContent.KEYPAD_CHEST, new net.geforcemods.securitycraft.renderers.KeypadChestItemRenderer());
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.CLAYMORE_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ClaymoreRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.RETINAL_SCANNER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.RetinalScannerRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.REINFORCED_MOVING_PISTON_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ReinforcedPistonHeadRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYPAD_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.LASER_BLOCK_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYCARD_READER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.USERNAME_LOGGER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.BLOCK_CHANGE_DETECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.PROJECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ProjectorRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURE_TRADING_STATION_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecureTradingStationRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(SECURITY_CAMERA_LOCATION, net.geforcemods.securitycraft.models.SecurityCameraModel::createLayer);
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.SECURITY_CAMERA_ENTITY, net.minecraft.client.renderer.entity.NoopRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURITY_CAMERA_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecurityCameraRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(SCContent.SECURITY_CAMERA_ITEM, new net.geforcemods.securitycraft.renderers.SecurityCameraItemRenderer());
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.UpdateLaserColorsPayload payload = net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.read(buf);

			client.execute(() -> {
				for (net.minecraft.core.BlockPos pos : payload.positions())
					client.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
			});
		});

		//the iron trapdoor has its own artwork, so upstream marks it hasReinforcedTint = false and leaves it untinted
		java.util.List<Block> tinted = new java.util.ArrayList<>(SCContent.REINFORCED_BLOCKS);

		tinted.remove(SCContent.REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"));
		//the grass block gets its own providers below so the grass overlay stays biome-green
		tinted.remove(SCContent.REINFORCED_BY_NAME.get("reinforced_grass_block"));

		Block[] reinforced = tinted.toArray(new Block[0]);
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? reinforcedTint() : -1, reinforced);

		//reinforced grass block: tint the grass overlay (tintindex 1) with the biome grass colour x the reinforced tint
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if (tintIndex == 1 && !state.getValue(net.minecraft.world.level.block.SnowyDirtBlock.SNOWY)) {
				int grass = view != null && pos != null ? net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(view, pos) : net.minecraft.world.level.GrassColor.get(0.5D, 1.0D);
				return net.minecraft.util.FastColor.ARGB32.multiply(0xFF000000 | grass, reinforcedTint());
			}

			return tintIndex == 0 ? reinforcedTint() : -1;
		}, SCContent.REINFORCED_BY_NAME.get("reinforced_grass_block"));

		// Glass panes use a flat (item/generated) icon; the grey tint on a translucent texture makes it near-invisible, so skip it for their items.
		java.util.List<Block> itemTinted = new java.util.ArrayList<>(tinted);
		itemTinted.removeAll(SCContent.GLASS_PANE_BLOCKS);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? reinforcedTint() : -1, itemTinted.toArray(new Block[0]));

		//reinforced grass block item: same grass-overlay tint, using the default grass colour
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			if (tintIndex == 1)
				return net.minecraft.util.FastColor.ARGB32.multiply(0xFF000000 | net.minecraft.world.level.GrassColor.get(0.5D, 1.0D), reinforcedTint());

			return tintIndex == 0 ? reinforcedTint() : -1;
		}, SCContent.REINFORCED_BY_NAME.get("reinforced_grass_block"));

		for (Block glass : SCContent.GLASS_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(glass, RenderType.translucent());

		for (Block cutout : SCContent.CUTOUT_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());

		//the door model carries NeoForge's "render_type": "cutout" field, which Fabric ignores, so its window
		//pane would render solid black instead of see-through
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_DOOR, RenderType.cutout());

		// Disguise: wrap disguisable block models so they can render as the disguised block (from the disguise module).
		java.util.Set<String> disguisableModelPaths = java.util.Set.of("laser_block", "keypad", "keycard_reader", "retinal_scanner", "username_logger", "block_change_detector", "projector", "secure_trading_station", "security_camera");
		net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((model, context) -> {
			net.minecraft.resources.ResourceLocation id = context.id();

			if (id != null && "securitycraft".equals(id.getNamespace()) && disguisableModelPaths.contains(id.getPath()) && !(id instanceof net.minecraft.client.resources.model.ModelResourceLocation mrl && "inventory".equals(mrl.getVariant())))
				return new net.geforcemods.securitycraft.models.DisguisableBakedModel(model);

			return model;
		}));

		// Block Change Detector: render bounding box highlights around detected block changes
		net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
			Minecraft mc = Minecraft.getInstance();
			net.minecraft.client.multiplayer.ClientLevel level = mc.level;

			if (level == null || mc.player == null)
				return;

			com.mojang.blaze3d.vertex.PoseStack pose = context.matrixStack();
			net.minecraft.world.phys.Vec3 camPos = context.camera().getPosition();

			for (net.minecraft.core.BlockPos bcdPos : net.geforcemods.securitycraft.misc.BlockEntityTracker.BLOCK_CHANGE_DETECTOR.getTrackedBlockEntities(level)) {
				net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(bcdPos);

				if (be instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity bcd && bcd.isShowingHighlights() && bcd.isOwnedBy(mc.player)) {
					int packedColor = bcd.getColor();
					float r = net.minecraft.util.FastColor.ARGB32.red(packedColor) / 255.0F;
					float g = net.minecraft.util.FastColor.ARGB32.green(packedColor) / 255.0F;
					float b = net.minecraft.util.FastColor.ARGB32.blue(packedColor) / 255.0F;
					com.mojang.blaze3d.vertex.VertexConsumer consumer = context.consumers() != null ? context.consumers().getBuffer(RenderType.lines()) : mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

					for (net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.ChangeEntry changeEntry : bcd.getFilteredEntries()) {
						net.minecraft.core.BlockPos pos = changeEntry.pos();

						pose.pushPose();
						pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
						net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, consumer, 0, 0, 0, 1, 1, 1, r, g, b, 1.0F);
						pose.popPose();
					}
				}
			}

			mc.renderBuffers().bufferSource().endBatch();
		});

		// Laser beam: translucent animated texture, tinted at tintindex 0 by the lens colour (red default).
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.LASER_FIELD, RenderType.translucent());
		ColorProviderRegistry.BLOCK.register(SecurityCraftClient::laserFieldColor, SCContent.LASER_FIELD);

		//the fake liquids have no textures of their own: they borrow water's and lava's, which is what makes them
		//indistinguishable from the real thing until something walks into them
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(SCContent.FAKE_WATER, SCContent.FLOWING_FAKE_WATER, new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_STILL, net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_FLOWING, net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_OVERLAY, 0xFF3F76E4));
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(SCContent.FAKE_LAVA, SCContent.FLOWING_FAKE_LAVA, new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(new net.minecraft.resources.ResourceLocation("block/lava_still"), new net.minecraft.resources.ResourceLocation("block/lava_flow")));
		BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), SCContent.FAKE_WATER, SCContent.FLOWING_FAKE_WATER);

		// Lens item: tinted by its dyed colour (uncolored lens stays untinted).
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 && net.geforcemods.securitycraft.util.Utils.hasLensColor(stack) ? net.geforcemods.securitycraft.util.Utils.getLensColor(stack) : -1, SCContent.LENS);

		net.geforcemods.securitycraft.misc.KeyBindings.init();
		net.geforcemods.securitycraft.entity.camera.CameraController.register();

		net.minecraft.client.renderer.item.ItemProperties.register(SCContent.CAMERA_MONITOR, SCContent.id("linking_state"), (stack, level, entity, id) -> {
			if (!(entity instanceof net.minecraft.world.entity.player.Player player))
				return EMPTY_STATE;

			float linkingState = getLinkingState(level, player, stack, bhr -> level.getBlockEntity(bhr.getBlockPos()) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity, 30, (tag, i) -> {
				if (!tag.contains("Camera" + i))
					return null;

				String camera = tag.getString("Camera" + i);

				return java.util.Arrays.stream(camera.substring(0, camera.lastIndexOf(' ')).split(" ")).map(Integer::parseInt).toArray(Integer[]::new);
			});

			if (!net.geforcemods.securitycraft.items.CameraMonitorItem.hasCameraAdded(stack.getTag())) {
				if (linkingState == NOT_LINKED_STATE)
					return NOT_LINKED_STATE;
				else
					return EMPTY_STATE;
			}
			else
				return linkingState;
		});

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.SetCameraViewPayload.CHANNEL, (client, handler, buf, responseSender) -> {
			net.geforcemods.securitycraft.network.SetCameraViewPayload payload = net.geforcemods.securitycraft.network.SetCameraViewPayload.read(buf);

			client.execute(() -> handleSetCameraView(client, payload));
		});

		net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
			if (isPlayerMountedOnCamera())
				cameraOverlay(guiGraphics, tickDelta);
		});

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (cameraInfoMessageTime >= 0)
				cameraInfoMessageTime--;
		});

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			net.geforcemods.securitycraft.entity.camera.CameraViewAreaExtension.onChunkUnload(chunk.getPos().x, chunk.getPos().z);
		});
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			net.geforcemods.securitycraft.entity.camera.FrameFeedHandler.removeAllFeeds();
			net.geforcemods.securitycraft.entity.camera.CameraClientChunkCacheExtension.clear();
			net.geforcemods.securitycraft.entity.camera.CameraViewAreaExtension.clear();
		});
	}
}
