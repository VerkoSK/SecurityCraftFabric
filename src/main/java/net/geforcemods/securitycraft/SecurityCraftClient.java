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

	public static final net.minecraft.resources.ResourceLocation NIGHT_VISION = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/mob_effect/night_vision.png");

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

	private static long cameraLastDayTime = -1;

	private static long cameraLastGameTime = -1;

	private static long cameraSimulatedOffset = 0;



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

			net.minecraft.network.chat.Component message = text.apply(options);

			int textColor = (whiteText.test(be) ? 0xFFFFFF : 0xFF0000) | (alpha << 24);



			guiGraphics.drawString(font, message, scaledWidth - font.width(message) - 8, scaledHeight - heightOffset, textColor, true);

		}

	}



	private static void cameraOverlay(net.minecraft.client.gui.GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {

		Minecraft mc = Minecraft.getInstance();

		net.minecraft.client.multiplayer.ClientLevel level = mc.level;

		net.minecraft.world.entity.player.Player player = mc.player;



		if (level == null || player == null)

			return;



		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);

		com.mojang.blaze3d.platform.Window window = mc.getWindow();

		int scaledWidth = window.getGuiScaledWidth();

		int scaledHeight = window.getGuiScaledHeight();

		net.minecraft.core.BlockPos pos = mc.cameraEntity.blockPosition();



		if (!(level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be))

			return;



		net.minecraft.client.gui.Font font = mc.font;

		net.minecraft.client.Options options = mc.options;

		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

		long rawDayTime = level.getDayTime();

		long gameTime = level.getGameTime();

		long dayTime;



		if (level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DAYLIGHT)) {

			cameraLastDayTime = rawDayTime;

			cameraLastGameTime = gameTime;

			cameraSimulatedOffset = 0;

			dayTime = rawDayTime;

		}

		else {

			if (rawDayTime != cameraLastDayTime || gameTime < cameraLastGameTime || cameraLastGameTime == -1) {

				cameraLastDayTime = rawDayTime;

				cameraLastGameTime = gameTime;

				cameraSimulatedOffset = 0;

			}

			else {

				long elapsed = gameTime - cameraLastGameTime;



				if (elapsed > 0) {

					cameraSimulatedOffset += elapsed;

					cameraLastGameTime = gameTime;

				}

			}



			dayTime = rawDayTime + cameraSimulatedOffset;

		}



		long normalizedDayTime = Math.floorMod(dayTime, 24000L);

		int hours24 = (int) ((normalizedDayTime / 1000L + 6L) % 24L);

		int hours = hours24 % 12;

		int minutes = (int) ((normalizedDayTime % 1000L) * 60L / 1000L);

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



							if (lens.has(net.minecraft.core.component.DataComponents.DYED_COLOR))

								return 0xFF000000 | lens.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb();



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

		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {

			net.minecraft.network.chat.Component title = context.client().level != null ? context.client().level.getBlockState(payload.pos()).getBlock().getName() : net.minecraft.network.chat.Component.empty();



			context.client().setScreen(payload.setup() ? new SetPasscodeScreen(payload.pos(), title) : new CheckPasscodeScreen(payload.pos(), title));

		}));

		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.BLOCK_REINFORCER_MENU, net.geforcemods.securitycraft.screen.BlockReinforcerScreen::new);

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.SyncLockedContainersPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			net.geforcemods.securitycraft.misc.ClientContainerLocks.handle(payload.clearExisting(), payload.locked(), payload.unlocked());
		}));

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> net.geforcemods.securitycraft.misc.ClientContainerLocks.clear());

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

		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.IMS_BOMB_ENTITY, net.geforcemods.securitycraft.renderers.IMSBombRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(net.geforcemods.securitycraft.renderers.IMSBombRenderer.IMS_BOMB_LOCATION, net.geforcemods.securitycraft.models.IMSBombModel::createLayer);

		//the chest's block model is empty, so both the placed block and the item are drawn by its own renderer

		//the secret signs draw their text only for the players allowed to read it

		registerSignRenderer(SCContent.SECRET_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretSignRenderer::new);

		registerSignRenderer(SCContent.SECRET_HANGING_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretHangingSignRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYPAD_CHEST_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.KeypadChestRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(SCContent.KEYPAD_CHEST, new net.geforcemods.securitycraft.renderers.KeypadChestItemRenderer());

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.CLAYMORE_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ClaymoreRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.RETINAL_SCANNER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.RetinalScannerRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.REINFORCED_MOVING_PISTON_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ReinforcedPistonHeadRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.SECURITY_CAMERA_ENTITY, net.minecraft.client.renderer.entity.NoopRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(SECURITY_CAMERA_LOCATION, net.geforcemods.securitycraft.models.SecurityCameraModel::createLayer);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURITY_CAMERA_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecurityCameraRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(SCContent.SECURITY_CAMERA_ITEM, new net.geforcemods.securitycraft.renderers.SecurityCameraItemRenderer());

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYPAD_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.LASER_BLOCK_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYCARD_READER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.USERNAME_LOGGER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.BLOCK_CHANGE_DETECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.PROJECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ProjectorRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURE_TRADING_STATION_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecureTradingStationRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.TYPE, (payload, context) -> context.client().execute(() -> {

			for (net.minecraft.core.BlockPos pos : payload.positions())

				context.client().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

		}));

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.RefreshDisguisableModelPayload.TYPE, (payload, context) -> context.client().execute(() -> {

			Minecraft mc = context.client();

			if (mc.level != null)

				mc.levelRenderer.setBlocksDirty(payload.pos().getX(), payload.pos().getY(), payload.pos().getZ(), payload.pos().getX(), payload.pos().getY(), payload.pos().getZ());

		}));

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.SetCameraViewPayload.TYPE, (payload, context) -> context.client().execute(() -> {

			Minecraft mc = context.client();

			if (mc.level == null)
				return;

			net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.id());
			boolean isMountingCamera = entity instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera;

			if (isMountingCamera || entity instanceof net.minecraft.world.entity.player.Player) {
				mc.setCameraEntity(entity);

				if (isMountingCamera) {
					net.geforcemods.securitycraft.entity.camera.CameraController.setCameraMountedTimestamp();
					resetCameraInfoMessageTime();
				}
			}

		}));

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.ToggleAlarmSoundPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null)
				payload.handle(mc.level);
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenAlarmScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.AlarmBlockEntity be)
				mc.setScreen(new net.geforcemods.securitycraft.screen.AlarmScreen(be, be.getSound().getLocation()));
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenUsernameLoggerScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)
				mc.setScreen(new net.geforcemods.securitycraft.screen.UsernameLoggerScreen(be));
		}));

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLoggerPayload.TYPE, (payload, context) -> context.client().execute(() -> {

			Minecraft mc = context.client();

			if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)

			{
				java.util.UUID uuid = null;
				try {
					uuid = java.util.UUID.fromString(payload.uuid());
				}
				catch (IllegalArgumentException ignored) {}
				be.getEntries()[payload.index()] = new net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity.UsernameLoggerEntry(payload.player(), uuid, payload.timestamp());
			}

		}));



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

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_BY_NAME.get("reinforced_glass"), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_BY_NAME.get("reinforced_glass_pane"), RenderType.cutoutMipped());



		for (Block cutout : SCContent.CUTOUT_BLOCKS)

			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());



		//the door model carries NeoForge's "render_type": "cutout" field, which Fabric ignores, so its window

		//pane would render solid black instead of see-through

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_DOOR, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.SECURITY_CAMERA, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.ALARM, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.MOTION_ACTIVATED_LIGHT, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.PROJECTOR, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.SECURE_TRADING_STATION, RenderType.cutout());

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.TRACK_MINE, RenderType.cutout());



		// Disguise: wrap the laser + keypad block models so they can render as the disguised block (from the disguise module).

		net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((model, context) -> {

			net.minecraft.client.resources.model.ModelResourceLocation id = context.topLevelId();



			if (id != null && (id.id().equals(SCContent.id("laser_block")) || id.id().equals(SCContent.id("keypad")) || id.id().equals(SCContent.id("username_logger")) || id.id().equals(SCContent.id("block_change_detector")) || id.id().equals(SCContent.id("projector"))))

				return new net.geforcemods.securitycraft.models.DisguisableBakedModel(model);



			return model;

		}));



		// Laser beam: translucent animated texture, tinted at tintindex 0 by the lens colour (red default).

		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.LASER_FIELD, RenderType.translucent());

		ColorProviderRegistry.BLOCK.register(SecurityCraftClient::laserFieldColor, SCContent.LASER_FIELD);



		//the fake liquids have no textures of their own: they borrow water's and lava's, which is what makes them

		//indistinguishable from the real thing until something walks into them

		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(SCContent.FAKE_WATER, SCContent.FLOWING_FAKE_WATER, new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_STILL, net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_FLOWING, net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_OVERLAY, 0xFF3F76E4));

		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(SCContent.FAKE_LAVA, SCContent.FLOWING_FAKE_LAVA, new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/lava_still"), net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/lava_flow")));

		BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), SCContent.FAKE_WATER, SCContent.FLOWING_FAKE_WATER);



		// Lens item: tinted by its dyed colour (uncolored lens stays untinted).

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 && stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR) ? stack.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb() : -1, SCContent.LENS);



		net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(context -> {

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



		net.geforcemods.securitycraft.items.properties.BlockLinked cameraProperty = new net.geforcemods.securitycraft.items.properties.BlockLinked(SCContent.BOUND_CAMERAS, net.geforcemods.securitycraft.items.properties.HitCheck.SECURITY_CAMERA);

		net.minecraft.client.renderer.item.ItemProperties.register(SCContent.CAMERA_MONITOR, SCContent.id("linking_state"), cameraProperty::get);



		net.geforcemods.securitycraft.misc.KeyBindings.init();

		net.geforcemods.securitycraft.entity.camera.CameraController.register();



		net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {

			if (isPlayerMountedOnCamera())

				cameraOverlay(guiGraphics, deltaTracker);

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

