package net.geforcemods.securitycraft.screen.components;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.world.item.ItemStack;

/**
 * Cycles through and draws a set of item stacks in one 16x16 slot, one at a time. Ported from upstream's class of
 * the same name; upstream fed it an {@code Ingredient}, but 1.21.6+ made {@code Ingredient} opaque, so this takes a
 * plain stack list instead.
 */
public class IngredientDisplay implements Renderable {
	private static final int DISPLAY_LENGTH = 20;
	private final int x;
	private final int y;
	private List<ItemStack> stacks = List.of();
	private int currentRenderingStack = 0;
	private float ticksToChange = DISPLAY_LENGTH;

	public IngredientDisplay(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		if (stacks.isEmpty())
			return;

		extractor.item(stacks.get(currentRenderingStack), x, y);
	}

	public void tick() {
		if (!hasShiftDown() && --ticksToChange <= 0) {
			changeRenderingStack(1);
			ticksToChange = DISPLAY_LENGTH;
		}
	}

	public void setStacks(List<ItemStack> stacks) {
		this.stacks = stacks == null ? List.of() : stacks;
		currentRenderingStack = 0;
		ticksToChange = DISPLAY_LENGTH;
	}

	public ItemStack getCurrentStack() {
		return currentRenderingStack >= 0 && currentRenderingStack < stacks.size() ? stacks.get(currentRenderingStack) : ItemStack.EMPTY;
	}

	public void changeRenderingStack(double direction) {
		if (stacks.isEmpty())
			return;

		currentRenderingStack += (int) Math.signum(direction);

		if (currentRenderingStack < 0)
			currentRenderingStack = stacks.size() - 1;
		else if (currentRenderingStack >= stacks.size())
			currentRenderingStack = 0;
	}

	private static boolean hasShiftDown() {
		com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();

		return InputConstants.isKeyDown(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
}
