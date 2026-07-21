package net.geforcemods.securitycraft.util;

import java.util.List;

import net.minecraft.client.renderer.Rect2i;

/** Screens implementing this expose extra rectangular areas (e.g. a popped-out colour chooser) that recipe-viewer overlays should avoid. */
public interface IHasExtraAreas {
	List<Rect2i> getExtraAreas();
}
