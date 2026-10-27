package net.geforcemods.securitycraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.commands.SCCommand;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;

/** Common entrypoint. Registers content, networking and the command on both client and server. */
public class SecurityCraft implements ModInitializer {
	public static final String MODID = "securitycraft";
	public static final String PORT_AUTHOR = "VerkoSK";
	public static final String PORT_REPO = "https://github.com/VerkoSK/SecurityCraftFabric";
	public static final String VERSION = FabricLoader.getInstance().getModContainer(MODID).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0.7");
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();
	/** The currently running server, or null when none is running. Fabric replacement for NeoForge's {@code ServerLifecycleHooks.getCurrentServer()}. */
	public static MinecraftServer SERVER;

	public static String getVersion() {
		return "v" + VERSION;
	}

	@Override
	public void onInitialize() {
		ConfigHandler.load();
		SCContent.init();
		NetworkHandler.registerPayloads();
		NetworkHandler.registerServerReceivers();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SCCommand.register(dispatcher));
		net.geforcemods.securitycraft.items.MineRemoteAccessToolItem.registerBindingCallback();
		net.geforcemods.securitycraft.items.UniversalOwnerChangerItem.registerUseCallback();
		net.geforcemods.securitycraft.items.UniversalKeyChangerItem.registerUseCallback();
		net.geforcemods.securitycraft.items.CodebreakerItem.registerUseCallback();
		net.geforcemods.securitycraft.misc.ContainerLockEnforcement.register();
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) handler.player.level();
			net.geforcemods.securitycraft.misc.ContainerLockData data = net.geforcemods.securitycraft.misc.ContainerLockData.get(level);
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(handler.player, new net.geforcemods.securitycraft.network.SyncLockedContainersPayload(true, data.getAllLockedPositions(), java.util.List.of()));
		});
		net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			net.geforcemods.securitycraft.misc.ContainerLockData data = net.geforcemods.securitycraft.misc.ContainerLockData.get(destination);
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new net.geforcemods.securitycraft.network.SyncLockedContainersPayload(true, data.getAllLockedPositions(), java.util.List.of()));
		});
		net.geforcemods.securitycraft.items.UniversalBlockModifierItem.registerUseCallback();
		net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!level.isClientSide())
				net.geforcemods.securitycraft.misc.BlockEntityTracker.BLOCK_CHANGE_DETECTOR.getBlockEntitiesInRange(level, pos).forEach(detector -> detector.log(player, net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.DetectionMode.BREAK, pos, state));
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
		//1.21.2+ only syncs unlocked recipes to the client, and the manual's recipe grid reads the client recipe book,
		//so unlock every SecurityCraft recipe on join - the manual would otherwise show empty grids on every page
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			java.util.List<net.minecraft.world.item.crafting.RecipeHolder<?>> scRecipes = server.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.id().identifier().getNamespace().equals(MODID)).toList();

			if (!scRecipes.isEmpty())
				handler.player.awardRecipes(scRecipes);
		});
		LOGGER.info("==================================================================");
		LOGGER.info(" SecurityCraft (Fabric port by {}) v{} initialized", PORT_AUTHOR, VERSION);
		LOGGER.info(" Repository: {}", PORT_REPO);
		LOGGER.info(" Original Forge mod by Geforce, bl4ckscor3, Redstone_Dubstep, ChainmailPickaxe");
		LOGGER.info("==================================================================");
	}
}
