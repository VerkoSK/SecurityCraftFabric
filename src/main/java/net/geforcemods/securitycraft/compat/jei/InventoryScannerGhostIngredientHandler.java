package net.geforcemods.securitycraft.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.inventory.OwnerRestrictedSlot;
import net.geforcemods.securitycraft.network.SetGhostSlotPayload;
import net.geforcemods.securitycraft.screen.InventoryScannerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventoryScannerGhostIngredientHandler implements IGhostIngredientHandler<InventoryScannerScreen> {
	@Override
	public <I> List<Target<I>> getTargetsTyped(InventoryScannerScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
		if (!screen.be.isOwnedBy(Minecraft.getInstance().player) || !(ingredient.getIngredient() instanceof ItemStack))
			return List.of();

		List<Target<I>> targets = new ArrayList<>();

		for (Slot slot : screen.getMenu().slots) {
			if (slot instanceof OwnerRestrictedSlot) {
				Rect2i area = new Rect2i(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 16, 16);

				targets.add(new Target<>() {
					@Override
					public Rect2i getArea() {
						return area;
					}

					@Override
					public void accept(I ingredient) {
						ItemStack stack = ((ItemStack) ingredient).copy();

						stack.setCount(1);
						screen.be.getContents().set(slot.index, stack);
						ClientPlayNetworking.send(SetGhostSlotPayload.CHANNEL, new SetGhostSlotPayload(slot.index, stack).write());
					}
				});
			}
		}

		return targets;
	}

	@Override
	public void onComplete() {}
}
