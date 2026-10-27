package net.geforcemods.securitycraft.compat.ium;

import net.fabricmc.loader.api.FabricLoader;

public class IumCompat {
	public static boolean isActive() {
		return FabricLoader.getInstance().isModLoaded("sodium");
	}
}
