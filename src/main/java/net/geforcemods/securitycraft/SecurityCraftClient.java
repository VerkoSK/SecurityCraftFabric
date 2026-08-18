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
		net.minecraft.client.gui.screens.MenuScreens.register(SCContent.SINGLE_LENS_MENU, net.geforcemods.securitycraft.screen.SingleLensScreen::new);
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.BOUNCING_BETTY_ENTITY, net.geforcemods.securitycraft.renderers.BouncingBettyRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SCContent.IMS_BOMB_ENTITY, net.geforcemods.securitycraft.renderers.IMSBombRenderer::new);
		net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(SCContent.CLAYMORE_BLOCK_ENTITY, net.geforcemods.securitycraft.renderers.ClaymoreRenderer::new);
		net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(net.geforcemods.securitycraft.models.IMSBombModel.LAYER_LOCATION, net.geforcemods.securitycraft.models.IMSBombModel::createBodyLayer);
		ClientPlayNetworking.registerGlobalReceiver(net.geforcemods.securitycraft.network.UpdateLaserColorsPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			for (net.minecraft.core.BlockPos pos : payload.positions())
				context.client().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
		}));

		//the iron trapdoor has its own artwork, so upstream marks it hasReinforcedTint = false and leaves it untinted
		java.util.List<Block> tinted = new java.util.ArrayList<>(SCContent.REINFORCED_BLOCKS);

		tinted.remove(SCContent.REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"));

		Block[] reinforced = tinted.toArray(new Block[0]);
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> tintIndex == 0 ? reinforcedTint() : -1, reinforced);
		// Reinforced item tints are baked into their items/ model definitions (minecraft:constant tint) in 1.21.5, since Fabric's ColorProviderRegistry.ITEM was removed.

		for (Block glass : SCContent.GLASS_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(glass, RenderType.translucent());

		for (Block cutout : SCContent.CUTOUT_BLOCKS)
			BlockRenderLayerMap.INSTANCE.putBlock(cutout, RenderType.cutout());

		//the door model carries NeoForge's "render_type": "cutout" field, which Fabric ignores, so its window
		//pane would render solid black instead of see-through
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.REINFORCED_DOOR, RenderType.cutout());

		// Disguise: wrap the laser + keypad block models so they can render as the disguised block (from the disguise module).
		net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyBlockModelAfterBake().register((model, context) -> {
			net.minecraft.world.level.block.Block block = context.state().getBlock();

			if (block == SCContent.LASER_BLOCK || block == SCContent.KEYPAD)
				return new net.geforcemods.securitycraft.models.DisguisableBakedModel(model);

			return model;
		}));

		// Laser beam: translucent animated texture, tinted at tintindex 0 by the lens colour (red default).
		BlockRenderLayerMap.INSTANCE.putBlock(SCContent.LASER_FIELD, RenderType.translucent());
		ColorProviderRegistry.BLOCK.register(SecurityCraftClient::laserFieldColor, SCContent.LASER_FIELD);

		// Lens item: tinted by its dyed colour via the minecraft:dye tint source in items/lens.json (Fabric's ColorProviderRegistry.ITEM was removed in 1.21.5).
	}
}
