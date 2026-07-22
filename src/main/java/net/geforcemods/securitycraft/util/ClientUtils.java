package net.geforcemods.securitycraft.util;

import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Client-only helpers ported from the original SecurityCraft {@code util.ClientUtils}. */
public class ClientUtils {
	private ClientUtils() {}

	/** Draws a module's texture (semi-transparent if not installed) and shows the given tooltip when hovered, matching the original. */
	public static void renderModuleInfo(GuiGraphics guiGraphics, Font font, ModuleType module, Component tooltip, boolean hasModule, int x, int y, int mouseX, int mouseY) {
		float alpha = hasModule ? 1.0F : 0.5F;

		// Blend must be enabled for the alpha in setColor to actually fade the icon (matches upstream renderModuleInfo).
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
		guiGraphics.blit(moduleTexture(module), x, y, 0.0F, 0.0F, 16, 16, 16, 16);

		if (module == ModuleType.REDSTONE)
			guiGraphics.blit(new net.minecraft.resources.ResourceLocation("textures/item/redstone.png"), x, y, 0.0F, 0.0F, 16, 16, 16, 16);
		else if (module == ModuleType.SPEED)
			guiGraphics.blit(new net.minecraft.resources.ResourceLocation("textures/item/sugar.png"), x, y, 0.0F, 0.0F, 16, 16, 16, 16);

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();

		if (tooltip != null && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY <= y + 16)
			guiGraphics.renderComponentTooltip(font, java.util.List.of(tooltip), mouseX, mouseY);
	}

	private static net.minecraft.resources.ResourceLocation moduleTexture(ModuleType module) {
		return net.geforcemods.securitycraft.SCContent.id(switch (module) {
			case ALLOWLIST -> "textures/item/whitelist_module.png";
			case DENYLIST -> "textures/item/blacklist_module.png";
			case HARMING -> "textures/item/harming_module.png";
			case SMART -> "textures/item/smart_module.png";
			case STORAGE -> "textures/item/storage_module.png";
			case DISGUISE -> "textures/item/disguise_module.png";
			default -> "textures/item/module_background.png"; // REDSTONE, SPEED
		});
	}

	/** Forces every rendered chunk to rebuild, so a live tint change is reflected immediately. */
	public static void recompileAllChunksInRange() {
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	/** Fills a rectangle with a left-to-right colour gradient (ARGB), drawn as 1px vertical strips. */
	public static void fillHorizontalGradient(GuiGraphics guiGraphics, int unused, int left, int top, int right, int bottom, int leftColor, int rightColor) {
		int width = right - left;

		if (width <= 0)
			return;

		for (int x = 0; x < width; x++) {
			float t = width == 1 ? 0.0F : (float) x / (width - 1);
			int color = lerpColor(leftColor, rightColor, t);

			guiGraphics.fill(left + x, top, left + x + 1, bottom, color);
		}
	}

	private static int lerpColor(int a, int b, float t) {
		int aa = a >>> 24, ar = a >> 16 & 0xFF, ag = a >> 8 & 0xFF, ab = a & 0xFF;
		int ba = b >>> 24, br = b >> 16 & 0xFF, bg = b >> 8 & 0xFF, bb = b & 0xFF;

		return Mth.lerpInt(t, aa, ba) << 24 | Mth.lerpInt(t, ar, br) << 16 | Mth.lerpInt(t, ag, bg) << 8 | Mth.lerpInt(t, ab, bb);
	}

	/** Standard HSB->RGB (0xRRGGBB), matching {@code java.awt.Color.HSBtoRGB} without the alpha byte. */
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;

		if (saturation == 0.0F)
			r = g = b = (int) (brightness * 255.0F + 0.5F);
		else {
			float h = (hue - (float) Math.floor(hue)) * 6.0F;
			float f = h - (float) Math.floor(h);
			float p = brightness * (1.0F - saturation);
			float q = brightness * (1.0F - saturation * f);
			float t = brightness * (1.0F - saturation * (1.0F - f));

			switch ((int) h) {
				case 0 -> {
					r = (int) (brightness * 255.0F + 0.5F);
					g = (int) (t * 255.0F + 0.5F);
					b = (int) (p * 255.0F + 0.5F);
				}
				case 1 -> {
					r = (int) (q * 255.0F + 0.5F);
					g = (int) (brightness * 255.0F + 0.5F);
					b = (int) (p * 255.0F + 0.5F);
				}
				case 2 -> {
					r = (int) (p * 255.0F + 0.5F);
					g = (int) (brightness * 255.0F + 0.5F);
					b = (int) (t * 255.0F + 0.5F);
				}
				case 3 -> {
					r = (int) (p * 255.0F + 0.5F);
					g = (int) (q * 255.0F + 0.5F);
					b = (int) (brightness * 255.0F + 0.5F);
				}
				case 4 -> {
					r = (int) (t * 255.0F + 0.5F);
					g = (int) (p * 255.0F + 0.5F);
					b = (int) (brightness * 255.0F + 0.5F);
				}
				case 5 -> {
					r = (int) (brightness * 255.0F + 0.5F);
					g = (int) (p * 255.0F + 0.5F);
					b = (int) (q * 255.0F + 0.5F);
				}
			}
		}

		return r << 16 | g << 8 | b;
	}

	/** Standard RGB->HSB, returning {hue, saturation, brightness}, matching {@code java.awt.Color.RGBtoHSB}. */
	public static float[] RGBtoHSB(int r, int g, int b) {
		float hue, saturation, brightness;
		int cmax = Math.max(r, Math.max(g, b));
		int cmin = Math.min(r, Math.min(g, b));

		brightness = cmax / 255.0F;
		saturation = cmax != 0 ? (float) (cmax - cmin) / cmax : 0.0F;

		if (saturation == 0.0F)
			hue = 0.0F;
		else {
			float redc = (float) (cmax - r) / (cmax - cmin);
			float greenc = (float) (cmax - g) / (cmax - cmin);
			float bluec = (float) (cmax - b) / (cmax - cmin);

			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0F + redc - bluec;
			else
				hue = 4.0F + greenc - redc;

			hue /= 6.0F;

			if (hue < 0.0F)
				hue += 1.0F;
		}

		return new float[] {
				hue, saturation, brightness
		};
	}
}
