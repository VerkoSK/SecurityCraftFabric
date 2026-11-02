package net.geforcemods.securitycraft.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.CameraMonitorItem;
import net.geforcemods.securitycraft.network.MountCameraPayload;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CameraMonitorScreen extends CameraSelectScreen {
	private final ItemStack stack;

	public CameraMonitorScreen(ItemStack stack) {
		super(stack.getOrDefault(SCContent.BOUND_CAMERAS, CameraMonitorItem.DEFAULT_NAMED_POSITIONS).positions(), false);
		this.stack = stack;
	}

	@Override
	protected void viewCamera(GlobalPos cameraPos) {
		ClientPlayNetworking.send(new MountCameraPayload(cameraPos.pos()));
		super.viewCamera(cameraPos);
	}

	@Override
	protected void unbindCamera(GlobalPos cameraPos) {
		CameraMonitorItem.removeCameraOnClient(cameraPos, stack);
	}

	@Override
	public boolean stillValid(Player player) {
		return !PlayerUtils.getItemStackFromAnyHand(player, stack.getItem()).isEmpty();
	}
}
