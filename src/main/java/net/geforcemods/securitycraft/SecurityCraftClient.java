package net.geforcemods.securitycraft;

import java.util.List;
import java.util.UUID;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.geforcemods.securitycraft.misc.TintMode;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;

/** Client entrypoint for SecurityCraft Fabric on 26.x. */
public class SecurityCraftClient implements ClientModInitializer {
	public static final Identifier CAMERA_DASHBOARD = SCContent.id("textures/gui/camera/camera_dashboard.png");
	public static final Identifier CAMERA_OVERLAY = SCContent.id("textures/gui/camera/camera_overlay.png");
	public static final Identifier NIGHT_VISION = SCContent.id("textures/gui/camera/night_vision.png");
	public static final net.minecraft.client.model.geom.ModelLayerLocation SECURITY_CAMERA_LOCATION = new net.minecraft.client.model.geom.ModelLayerLocation(SCContent.id("security_camera"), "main");
	public static final net.minecraft.client.model.geom.ModelLayerLocation IMS_BOMB_LOCATION = new net.minecraft.client.model.geom.ModelLayerLocation(SCContent.id("ims_bomb"), "main");

	private static final CameraKeyInfoEntry[] CAMERA_KEY_INFO_LIST = {
			new CameraKeyInfoEntry(() -> net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomIn.isDown(), opt -> Component.translatable("gui.securitycraft:camera.zoomIn", net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomIn.getTranslatedKeyMessage()), be -> true),
			new CameraKeyInfoEntry(() -> net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomOut.isDown(), opt -> Component.translatable("gui.securitycraft:camera.zoomOut", net.geforcemods.securitycraft.misc.KeyBindings.cameraZoomOut.getTranslatedKeyMessage()), be -> true),
			new CameraKeyInfoEntry(() -> net.geforcemods.securitycraft.misc.KeyBindings.cameraEmitRedstone.isDown(), opt -> Component.translatable("gui.securitycraft:camera.emitRedstone", net.geforcemods.securitycraft.misc.KeyBindings.cameraEmitRedstone.getTranslatedKeyMessage()), be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.REDSTONE)),
			new CameraKeyInfoEntry(() -> net.geforcemods.securitycraft.misc.KeyBindings.cameraActivateNightVision.isDown(), opt -> Component.translatable("gui.securitycraft:camera.activateNightVision", net.geforcemods.securitycraft.misc.KeyBindings.cameraActivateNightVision.getTranslatedKeyMessage()), be -> true),
			new CameraKeyInfoEntry(() -> net.geforcemods.securitycraft.misc.KeyBindings.setDefaultViewingDirection.isDown(), opt -> Component.translatable("gui.securitycraft:camera.setDefaultViewingDirection", net.geforcemods.securitycraft.misc.KeyBindings.setDefaultViewingDirection.getTranslatedKeyMessage()), be -> be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.SMART))
	};

	private static int cameraInfoMessageTime = -1;
	private static long cameraLastDayTime = -1;
	private static long cameraLastGameTime = -1;
	private static long cameraSimulatedOffset = 0;

	public static boolean isPlayerMountedOnCamera() {
		return Minecraft.getInstance().getCameraEntity() instanceof net.geforcemods.securitycraft.entity.camera.SecurityCamera;
	}

	public static void displayCameraMonitorScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.CameraMonitorScreen(stack));
	}

	public static void resetCameraInfoMessageTime() {
		cameraInfoMessageTime = 200;
	}

	public record CameraKeyInfoEntry(java.util.function.Supplier<Boolean> enabled, java.util.function.Function<net.minecraft.client.Options, Component> text, java.util.function.Predicate<net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity> whiteText) {
		public void drawString(net.minecraft.client.Options options, GuiGraphicsExtractor extractor, net.minecraft.client.gui.Font font, int scaledWidth, int scaledHeight, int heightOffset, net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be, int alpha) {
			Component message = text.apply(options);
			int textColor = (whiteText.test(be) ? 0xFFFFFF : 0xFF0000) | (alpha << 24);
			extractor.text(font, message, scaledWidth - font.width(message) - 8, scaledHeight - heightOffset, textColor, true);
		}
	}

	private static void cameraOverlay(GuiGraphicsExtractor extractor, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		net.minecraft.client.multiplayer.ClientLevel level = mc.level;
		net.minecraft.world.entity.player.Player player = mc.player;

		if (level == null || player == null)
			return;

		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
		int scaledWidth = extractor.guiWidth();
		int scaledHeight = extractor.guiHeight();
		BlockPos pos = mc.getCameraEntity().blockPosition();

		if (!(level.getBlockEntity(pos) instanceof net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity be))
			return;

		// Lens tint
		net.minecraft.world.item.ItemStack lens = be.getLensContainer().getItem(0);
		if (lens.has(DataComponents.DYED_COLOR)) {
			extractor.fill(0, 0, scaledWidth, scaledHeight, lens.get(DataComponents.DYED_COLOR).rgb() + (be.getOpacity() << 24));
		}

		net.minecraft.client.gui.Font font = mc.font;
		net.minecraft.client.Options options = mc.options;
		BlockState state = level.getBlockState(pos);
		long rawDayTime = level.getOverworldClockTime();
		long gameTime = level.getGameTime();
		long dayTime;

		if (rawDayTime < 0) {
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
			Component cameraName = be.getCustomName();
			extractor.text(font, cameraName, scaledWidth - font.width(cameraName) - 8, 25, 0xFFFFFFFF, true);
			timeY += 10;
		}

		extractor.text(font, time, scaledWidth - font.width(time) - 4, timeY, 0xFFFFFFFF, true);

		if (cameraInfoMessageTime >= 0) {
			float fadeOutPartialTick = Math.max(cameraInfoMessageTime + 1.0F - partialTicks, 1.0F);
			int alpha = (int) Math.ceil(255.0F * Math.min(20.0F, fadeOutPartialTick) / 20.0F);
			int heightOffset = 10;

			for (int i = CAMERA_KEY_INFO_LIST.length - 1; i >= 0; i--) {
				CameraKeyInfoEntry entry = CAMERA_KEY_INFO_LIST[i];
				if (entry.enabled().get()) {
					entry.drawString(options, extractor, font, scaledWidth, scaledHeight, heightOffset, be, alpha);
					heightOffset += 10;
				}
			}
		}

		extractor.blit(RenderPipelines.GUI_TEXTURED, CAMERA_DASHBOARD, 5, 0, 0.0F, 0.0F, 90, 20, 256, 256);
		extractor.blit(RenderPipelines.GUI_TEXTURED, CAMERA_DASHBOARD, scaledWidth - 70, 5, 190.0F, 0.0F, 65, 30, 256, 256);

		if (mc.player != null && !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION))
			extractor.blit(RenderPipelines.GUI_TEXTURED, CAMERA_DASHBOARD, 28, 4, 90.0F, 12.0F, 16, 11, 256, 256);
		else
			extractor.blit(RenderPipelines.GUI_TEXTURED, NIGHT_VISION, 27, -1, 0.0F, 0.0F, 18, 18, 18, 18);

		if (state.hasProperty(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.FACING) && state.getSignal(level, pos, state.getValue(net.geforcemods.securitycraft.blocks.SecurityCameraBlock.FACING)) == 0) {
			if (!be.isModuleEnabled(net.geforcemods.securitycraft.misc.ModuleType.REDSTONE))
				net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.NOT_INSTALLED.render(extractor, 12, 2);
			else
				net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.DEACTIVATED.render(extractor, 12, 2);
		}
		else
			net.geforcemods.securitycraft.misc.CameraRedstoneModuleState.ACTIVATED.render(extractor, 12, 2);
	}

	public static void openEditModuleScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.EditModuleScreen(stack));
	}

	public static void openManualScreen() {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.SCManualScreen());
	}

	public static void openMRATScreen(net.minecraft.world.item.ItemStack stack) {
		Minecraft.getInstance().setScreen(new net.geforcemods.securitycraft.screen.MineRemoteAccessToolScreen(stack));
	}

	private static int reinforcedTint() {
		return TintMode.tint(Minecraft.getInstance().player, 0xFFFFFFFF, null);
	}

	private static int laserFieldColor(BlockState state, BlockAndTintGetter view, BlockPos pos) {
		if (view != null && pos != null) {
			Direction axis = net.geforcemods.securitycraft.blocks.LaserFieldBlock.getFieldDirection(state);
			if (axis != null) {
				for (Direction dir : new Direction[] { axis, axis.getOpposite() }) {
					for (int i = 1; i <= ConfigHandler.laserBlockRange; i++) {
						BlockPos offsetPos = pos.relative(dir, i);
						if (view.getBlockEntity(offsetPos) instanceof net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity laser) {
							net.minecraft.world.item.ItemStack lens = laser.getLensContainer().getItem(dir.getOpposite().ordinal());
							if (lens.has(DataComponents.DYED_COLOR))
								return 0xFF000000 | lens.get(DataComponents.DYED_COLOR).rgb();
							return 0xFFFFFFFF;
						}
						BlockState offsetState = view.getBlockState(offsetPos);
						if (offsetState.getBlock() != SCContent.LASER_FIELD && !offsetState.isAir())
							break;
					}
				}
			}
		}
		return 0xFFFFFFFF;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> provider) {
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register((net.minecraft.world.level.block.entity.BlockEntityType) type, (net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider) provider);
	}

	@Override
	public void onInitializeClient() {
		net.geforcemods.securitycraft.items.properties.HitCheck.bootstrap();
		net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties.ID_MAPPER.put(SCContent.id("block_linked"), net.geforcemods.securitycraft.items.properties.BlockLinked.TYPE);
		net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties.ID_MAPPER.put(SCContent.id("codebreaker_state"), net.geforcemods.securitycraft.items.properties.CodebreakerState.TYPE);
		net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.ID_MAPPER.put(SCContent.id("keycard_count"), net.geforcemods.securitycraft.items.properties.KeycardCount.MAP_CODEC);

		SCClientConfig.load();
		net.geforcemods.securitycraft.misc.UpdateChecker.run();

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
			java.util.function.Consumer<Component> messageSink = message -> {
				if (client.player != null)
					client.player.sendSystemMessage(message);
			};
			net.geforcemods.securitycraft.misc.WelcomeMessage.show(messageSink);
			net.geforcemods.securitycraft.misc.UpdateChecker.notify(messageSink);
		}));

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Component title = context.client().level != null ? context.client().level.getBlockState(payload.pos()).getBlock().getName() : Component.empty();
			context.client().setScreen(payload.setup() ? new net.geforcemods.securitycraft.screen.SetPasscodeScreen(payload.pos(), title) : new net.geforcemods.securitycraft.screen.CheckPasscodeScreen(payload.pos(), title));
		}));

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

		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.SyncLockedContainersPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			net.geforcemods.securitycraft.misc.ClientContainerLocks.handle(payload.clearExisting(), payload.locked(), payload.unlocked());
		}));
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> net.geforcemods.securitycraft.misc.ClientContainerLocks.clear());

		// Entities & Renderers
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.BOUNCING_BETTY_ENTITY, net.geforcemods.securitycraft.renderers.BouncingBettyRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.IMS_BOMB_ENTITY, net.geforcemods.securitycraft.renderers.IMSBombRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(IMS_BOMB_LOCATION, net.geforcemods.securitycraft.models.IMSBombModel::createLayer);

		registerBlockEntityRenderer(SCContent.SECRET_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretSignRenderer::new);
		registerBlockEntityRenderer(SCContent.SECRET_HANGING_SIGN_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecretHangingSignRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_CHEST_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.KeypadChestRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.CLAYMORE_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ClaymoreRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.RETINAL_SCANNER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.RetinalScannerRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.REINFORCED_MOVING_PISTON_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ReinforcedPistonHeadRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.SECURITY_CAMERA_ENTITY, net.minecraft.client.renderer.entity.NoopRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(SECURITY_CAMERA_LOCATION, net.geforcemods.securitycraft.models.SecurityCameraModel::createLayer);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURITY_CAMERA_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecurityCameraRenderer::new);

		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYPAD_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.LASER_BLOCK_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.KEYCARD_READER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.USERNAME_LOGGER_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.BLOCK_CHANGE_DETECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.PROJECTOR_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ProjectorRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(SCContent.SECURE_TRADING_STATION_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.SecureTradingStationRenderer::new);

		// Networking payloads
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			for (BlockPos pos : payload.positions())
				context.client().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.RefreshDisguisableModelPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null)
				mc.levelRenderer.setBlocksDirty(payload.pos().getX(), payload.pos().getY(), payload.pos().getZ(), payload.pos().getX(), payload.pos().getY(), payload.pos().getZ());
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.SetCameraViewPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level == null) return;
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
				mc.setScreen(new net.geforcemods.securitycraft.screen.AlarmScreen(be, be.getSound().location()));
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.OpenUsernameLoggerScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be)
				mc.setScreen(new net.geforcemods.securitycraft.screen.UsernameLoggerScreen(be));
		}));
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLoggerPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			Minecraft mc = context.client();
			if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity be) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(payload.uuid());
				} catch (IllegalArgumentException ignored) {}
				be.getEntries()[payload.index()] = new net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity.UsernameLoggerEntry(payload.player(), uuid, payload.timestamp());
			}
		}));

		// Block colors
		List<Block> tinted = new java.util.ArrayList<>(SCContent.REINFORCED_BLOCKS);
		tinted.remove(SCContent.REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"));
		tinted.remove(SCContent.REINFORCED_BY_NAME.get("reinforced_grass_block"));

		Block[] reinforced = tinted.toArray(new Block[0]);
		BlockTintSource reinforcedTintSource = new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return reinforcedTint();
			}
			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter view, BlockPos pos) {
				return reinforcedTint();
			}
		};
		BlockColorRegistry.register(List.of(reinforcedTintSource), reinforced);

		BlockTintSource grassOverlayTintSource = new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return state.getValue(net.minecraft.world.level.block.SnowyBlock.SNOWY) ? reinforcedTint() : net.minecraft.util.ARGB.multiply(0xFF000000 | net.minecraft.world.level.GrassColor.get(0.5D, 1.0D), reinforcedTint());
			}
			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter view, BlockPos pos) {
				return state.getValue(net.minecraft.world.level.block.SnowyBlock.SNOWY) ? reinforcedTint() : net.minecraft.util.ARGB.multiply(0xFF000000 | net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(view, pos), reinforcedTint());
			}
		};
		BlockColorRegistry.register(List.of(reinforcedTintSource, grassOverlayTintSource), SCContent.REINFORCED_BY_NAME.get("reinforced_grass_block"));

		// Model disguise loading plugin
		net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyBlockModelAfterBake().register((model, context) -> {
			Block block = context.state().getBlock();
			if (block == SCContent.LASER_BLOCK || block == SCContent.KEYPAD || block instanceof net.geforcemods.securitycraft.api.IDisguisable || block instanceof net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock || block instanceof net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDispenserBlock || block instanceof net.geforcemods.securitycraft.blocks.reinforced.ReinforcedHopperBlock)
				return new net.geforcemods.securitycraft.models.DisguisableBakedModel(model);
			return model;
		}));

		// Laser beam tint
		BlockTintSource laserTintSource = new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFFFFFFFF;
			}
			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter view, BlockPos pos) {
				return laserFieldColor(state, view, pos);
			}
		};
		BlockColorRegistry.register(List.of(laserTintSource), SCContent.LASER_FIELD);

		// Fake fluids
		BlockTintSource waterTintSource = new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFFFFFFFF;
			}
			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter view, BlockPos pos) {
				return net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(view, pos) | 0xFF000000;
			}
		};
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(SCContent.FAKE_WATER, SCContent.FLOWING_FAKE_WATER, new net.minecraft.client.renderer.block.FluidModel.Unbaked(
				new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/water_still"), true),
				new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/water_flow"), true),
				new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
				waterTintSource));
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(SCContent.FAKE_LAVA, SCContent.FLOWING_FAKE_LAVA, new net.minecraft.client.renderer.block.FluidModel.Unbaked(
				new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/lava_still")),
				new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/lava_flow")),
				null,
				state -> 0xFFFFFFFF));

		// World render events for BCD highlights
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineState) -> {
			boolean dummy = true;
			Minecraft mc = Minecraft.getInstance();
			net.minecraft.client.multiplayer.ClientLevel level = mc.level;
			if (level == null || mc.player == null) return true;

			com.mojang.blaze3d.vertex.PoseStack pose = context.poseStack();
			net.minecraft.world.phys.Vec3 camPos = mc.gameRenderer.getMainCamera().position();

			for (BlockPos bcdPos : net.geforcemods.securitycraft.misc.BlockEntityTracker.BLOCK_CHANGE_DETECTOR.getTrackedBlockEntities(level)) {
				net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(bcdPos);
				if (be instanceof net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity bcd && bcd.isShowingHighlights() && bcd.isOwnedBy(mc.player)) {
					int packedColor = bcd.getColor();

					for (net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.ChangeEntry changeEntry : bcd.getFilteredEntries()) {
						BlockPos p = changeEntry.pos();
						pose.pushPose();
						pose.translate(p.getX() - camPos.x, p.getY() - camPos.y, p.getZ() - camPos.z);
						context.submitNodeCollector().submitCustomGeometry(pose, net.minecraft.client.renderer.rendertype.RenderTypes.lines(), (pPose, consumer) -> {
							com.mojang.blaze3d.vertex.PoseStack ps = new com.mojang.blaze3d.vertex.PoseStack();
							ps.mulPose(pPose.pose());
							net.minecraft.client.renderer.ShapeRenderer.renderShape(ps, consumer, net.minecraft.world.phys.shapes.Shapes.block(), 0.0D, 0.0D, 0.0D, packedColor, 1.0F);
						});
						pose.popPose();
					}
				}
			}
			return true;
		});

		net.geforcemods.securitycraft.misc.KeyBindings.init();
		net.geforcemods.securitycraft.entity.camera.CameraController.register();

		// HUD element registry for camera overlay
		HudElementRegistry.addLast(SCContent.id("camera_overlay"), (extractor, deltaTracker) -> {
			if (isPlayerMountedOnCamera())
				cameraOverlay(extractor, deltaTracker);
		});

		// Suppress mob effects on camera view
		HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS, original -> (extractor, deltaTracker) -> {
			if (!isPlayerMountedOnCamera())
				original.extractRenderState(extractor, deltaTracker);
		});

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (cameraInfoMessageTime >= 0)
				cameraInfoMessageTime--;
		});

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			net.geforcemods.securitycraft.entity.camera.CameraViewAreaExtension.onChunkUnload(chunk.getPos().x(), chunk.getPos().z());
		});

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			net.geforcemods.securitycraft.entity.camera.FrameFeedHandler.removeAllFeeds();
			net.geforcemods.securitycraft.entity.camera.CameraClientChunkCacheExtension.clear();
			net.geforcemods.securitycraft.entity.camera.CameraViewAreaExtension.clear();
		});
	}
}
