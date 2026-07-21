package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.api.IOwnable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;

/**
 * Controls under which condition a placed reinforced block is tinted, and holds the live tint state
 * (colour + mode) used by the block/item colour providers. Ported from the original SecurityCraft.
 */
public enum TintMode {
	ALL("all"),
	NONE("none") {
		@Override
		public boolean shouldTint(Player player, IOwnable ownable) {
			return false;
		}
	},
	OWNED("owned") {
		@Override
		public boolean shouldTint(Player player, IOwnable ownable) {
			return ownable != null && ownable.isOwnedBy(player);
		}
	},
	UNOWNED("unowned") {
		@Override
		public boolean shouldTint(Player player, IOwnable ownable) {
			return ownable == null || !ownable.isOwnedBy(player);
		}
	};

	private static int currentTintColor = 0x999999;
	private static TintMode currentTintMode = ALL;
	private final String translationKey;

	TintMode(String name) {
		translationKey = "gui.securitycraft:blockReinforcer.tintMode." + name;
	}

	public boolean shouldTint(Player player, IOwnable ownable) {
		return true;
	}

	/** Computes the tint applied at render time: base texture colour multiplied by the configured colour, or unchanged when the mode says not to tint. */
	public static int tint(Player player, int baseTint, IOwnable ownable) {
		if (mode() == NONE)
			return baseTint;

		if (mode().shouldTint(player, ownable))
			return ARGB.multiply(baseTint, 0xFF000000 | color());

		return baseTint;
	}

	public final Component translate() {
		return Component.translatable(translationKey);
	}

	public final Component tooltip() {
		return Component.translatable(translationKey + ".tooltip");
	}

	public static int color() {
		return currentTintColor;
	}

	public static TintMode mode() {
		return currentTintMode;
	}

	public static void setColor(int tintColor) {
		currentTintColor = tintColor;
	}

	public static void setMode(TintMode tintMode) {
		currentTintMode = tintMode;
	}
}
