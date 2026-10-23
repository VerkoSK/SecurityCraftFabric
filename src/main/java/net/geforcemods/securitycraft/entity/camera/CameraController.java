package net.geforcemods.securitycraft.entity.camera;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.DismountCameraPayload;
import net.geforcemods.securitycraft.network.SetCameraPoweredPayload;
import net.geforcemods.securitycraft.network.SetDefaultCameraViewingDirectionPayload;
import net.geforcemods.securitycraft.network.ToggleNightVisionPayload;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CameraController {
	public static CameraType previousCameraType;
	public static boolean resetOverlaysAfterDismount = false;
	private static ViewMovementKeyHandler[] moveKeyHandlers;

	private static ViewMovementKeyHandler[] getMoveKeyHandlers() {
		if (moveKeyHandlers == null) {
			Minecraft mc = Minecraft.getInstance();

			if (mc != null && mc.options != null) {
				moveKeyHandlers = new ViewMovementKeyHandler[] {
					new ViewMovementKeyHandler(mc.options.keyUp, CameraController::moveViewUp),
					new ViewMovementKeyHandler(mc.options.keyDown, CameraController::moveViewDown),
					new ViewMovementKeyHandler(mc.options.keyLeft, cam -> moveViewHorizontally(cam, cam.getYRot() - getMovementSpeed(cam) * cam.getZoomAmount())),
					new ViewMovementKeyHandler(mc.options.keyRight, cam -> moveViewHorizontally(cam, cam.getYRot() + getMovementSpeed(cam) * cam.getZoomAmount()))
				};
			}
			else
				return new ViewMovementKeyHandler[0];
		}

		return moveKeyHandlers;
	}
	private static int screenshotSoundCooldown = 0;
	private static long lastCameraMountTimestamp = 0;

	private CameraController() {}

	public static void register() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> onClientTickStart());
		ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTickEnd());
	}

	public static void onClientTickStart() {
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;

		if (cameraEntity instanceof SecurityCamera) {
			Options options = Minecraft.getInstance().options;

			for (ViewMovementKeyHandler handler : getMoveKeyHandlers()) {
				handler.tickStart();
			}

			if (options.keyShift.isDown()) {
				dismount();
				options.keyShift.setDown(false);
			}
		}
	}

	public static void onClientTickEnd() {
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;

		if (cameraEntity instanceof SecurityCamera cam) {
			for (ViewMovementKeyHandler handler : getMoveKeyHandlers()) {
				handler.tickEnd(cam);
			}

			if (KeyBindings.cameraZoomIn.isDown())
				zoomIn(cam);
			else if (KeyBindings.cameraZoomOut.isDown())
				zoomOut(cam);
			else
				cam.zooming = false;

			KeyBindings.cameraEmitRedstone.tick(cam);
			KeyBindings.cameraActivateNightVision.tick(cam);
			KeyBindings.setDefaultViewingDirection.tick(cam);
			screenshotSoundCooldown--;

			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				double yRotChange = player.getYRot() - player.yRotLast;
				double xRotChange = player.getXRot() - player.xRotLast;

				if (yRotChange != 0.0D || xRotChange != 0.0D)
					player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
			}
		}
		else if (resetOverlaysAfterDismount) {
			resetOverlaysAfterDismount = false;
		}
	}

	private static void dismount() {
		ClientPlayNetworking.send(new DismountCameraPayload());
	}

	public static void moveViewUp(SecurityCamera cam) {
		float next = cam.getXRot() - getMovementSpeed(cam) * cam.getZoomAmount();

		if (cam.isCameraDown()) {
			if (next > 40F)
				cam.setRotation(cam.getYRot(), next);
		}
		else if (next > -25F)
			cam.setRotation(cam.getYRot(), next);
	}

	public static void moveViewDown(SecurityCamera cam) {
		float next = cam.getXRot() + getMovementSpeed(cam) * cam.getZoomAmount();

		if (cam.isCameraDown()) {
			if (next < 90F)
				cam.setRotation(cam.getYRot(), next);
		}
		else if (next < 60F)
			cam.setRotation(cam.getYRot(), next);
	}

	public static void moveViewHorizontally(SecurityCamera cam, float next) {
		BlockState state = cam.level().getBlockState(cam.blockPosition());

		if (state.hasProperty(SecurityCameraBlock.FACING)) {
			float checkNext = next;

			if (checkNext < 0)
				checkNext += 360;

			boolean shouldSetRotation = switch (state.getValue(SecurityCameraBlock.FACING)) {
				case NORTH -> checkNext > 90F && checkNext < 270F;
				case SOUTH -> checkNext > 270F || checkNext < 90F;
				case EAST -> checkNext > 180F && checkNext < 360F;
				case WEST -> checkNext > 0F && checkNext < 180F;
				case DOWN -> true;
				default -> false;
			};

			if (shouldSetRotation)
				cam.setYRot(next);
		}
	}

	public static void zoomIn(SecurityCamera cam) {
		if (!cam.zooming)
			Minecraft.getInstance().level.playLocalSound(cam.blockPosition(), SCSounds.CAMERAZOOMIN.event, SoundSource.BLOCKS, 1.0F, 1.0F, true);

		cam.zooming = true;
		cam.setZoomAmount(Math.max(cam.getZoomAmount() - 0.1F, 0.1F));
	}

	public static void zoomOut(SecurityCamera cam) {
		if (!cam.zooming)
			Minecraft.getInstance().level.playLocalSound(cam.blockPosition(), SCSounds.CAMERAZOOMIN.event, SoundSource.BLOCKS, 1.0F, 1.0F, true);

		cam.zooming = true;
		cam.setZoomAmount(Math.min(cam.getZoomAmount() + 0.1F, 1.4F));
	}

	public static void toggleRedstone(SecurityCamera cam) {
		BlockPos pos = cam.blockPosition();
		Level level = cam.level();

		if (level.getBlockEntity(pos) instanceof IModuleInventory be && be.isModuleEnabled(ModuleType.REDSTONE))
			ClientPlayNetworking.send(new SetCameraPoweredPayload(pos, !level.getBlockState(pos).getValue(SecurityCameraBlock.POWERED)));
	}

	public static void toggleNightVision(SecurityCamera cam) {
		if (ConfigHandler.allowCameraNightVision)
			ClientPlayNetworking.send(new ToggleNightVisionPayload());
	}

	public static void setDefaultViewingDirection(SecurityCamera cam) {
		ClientPlayNetworking.send(new SetDefaultCameraViewingDirectionPayload(cam));
	}

	public static float getMovementSpeed(SecurityCamera cam) {
		SecurityCameraBlockEntity be = cam.getBlockEntity();

		if (be != null)
			return (float) be.getMovementSpeed();

		return 0.0F;
	}

	public static void setCameraMountedTimestamp() {
		lastCameraMountTimestamp = System.currentTimeMillis();
	}

	public static long getMillisSinceLastMount() {
		return System.currentTimeMillis() - lastCameraMountTimestamp;
	}

	public static class ViewMovementKeyHandler {
		private final KeyMapping key;
		private final Consumer<SecurityCamera> action;
		private boolean wasPressed;

		public ViewMovementKeyHandler(KeyMapping key, Consumer<SecurityCamera> action) {
			this.key = key;
			this.action = action;
		}

		public void tickStart() {
			wasPressed = key.isDown();

			if (wasPressed)
				key.setDown(false);
		}

		public void tickEnd(SecurityCamera cam) {
			if (wasPressed) {
				action.accept(cam);
				key.setDown(true);
			}
		}
	}
}
