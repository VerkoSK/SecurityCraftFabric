package net.geforcemods.securitycraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.geforcemods.securitycraft.commands.SCCommand;
import net.geforcemods.securitycraft.network.NetworkHandler;

/** Common entrypoint. Registers content, networking and the command on both client and server. */
public class SecurityCraft implements ModInitializer {
	public static final String MODID = "securitycraft";
	public static final String VERSION = "1.10.2.1-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		ConfigHandler.load();
		SCContent.init();
		NetworkHandler.registerPayloads();
		NetworkHandler.registerServerReceivers();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SCCommand.register(dispatcher));
		LOGGER.info("SecurityCraft (Fabric port) v{} initialized", VERSION);
	}
}
