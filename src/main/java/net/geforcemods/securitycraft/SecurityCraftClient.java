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
		ClientPlayNetworking.registerGlobalReceiver(OpenKeypadScreenPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			net.minecraft.network.chat.Component title = context.client().level != null ? context.client().level.getBlockState(payload.pos()).getBlock().getName() : net.minecraft.network.chat.Component.empty();

			context.client().setScreen(payload.setup() ? new SetPasscodeScreen(payload.pos(), title) : new CheckPasscodeScreen(payload.pos(), title));
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
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			for (net.minecraft.core.BlockPos pos : payload.positions())
				context.client().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
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

		for (Block cutout : SCContent.CUTOUT_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());

		//the door model carries NeoForge's "render_type": "cutout" field, which Fabric ignores, so its window
		//pane would render solid black instead of see-through
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_DOOR, RenderType.cutout());

		// Disguise: wrap the laser + keypad block models so they can render as the disguised block (from the disguise module).
		net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((model, context) -> {
			net.minecraft.client.resources.model.ModelResourceLocation id = context.topLevelId();

			if (id != null && (id.id().equals(SCContent.id("laser_block")) || id.id().equals(SCContent.id("keypad"))))
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
	}
}
