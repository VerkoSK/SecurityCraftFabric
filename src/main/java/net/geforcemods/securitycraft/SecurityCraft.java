package net.geforcemods.securitycraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.geforcemods.securitycraft.commands.SCCommand;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;

/** Common entrypoint. Registers content, networking and the command on both client and server. */
public class SecurityCraft implements ModInitializer {
	public static final String MODID = "securitycraft";
	public static final String VERSION = "1.10.2.1-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	/** The currently running server, or null when none is running. Fabric replacement for NeoForge's {@code ServerLifecycleHooks.getCurrentServer()}. */
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ConfigHandler.load();
		SCContent.init();
		NetworkHandler.registerPayloads();
		NetworkHandler.registerServerReceivers();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SCCommand.register(dispatcher));
		net.geforcemods.securitycraft.items.MineRemoteAccessToolItem.registerBindingCallback();
		net.geforcemods.securitycraft.items.UniversalOwnerChangerItem.registerUseCallback();
		net.geforcemods.securitycraft.items.UniversalBlockModifierItem.registerUseCallback();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
		LOGGER.info("SecurityCraft (Fabric port) v{} initialized", VERSION);
	}
}
