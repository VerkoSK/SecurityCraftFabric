package net.geforcemods.securitycraft.entity.camera;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stub implementation of FrameFeedHandler for V0.7. Full frame camera feed rendering is implemented in later versions with the Frame block.
 */
public class FrameFeedHandler {
	private FrameFeedHandler() {}

	public static void captureFrameFeeds(net.minecraft.client.DeltaTracker deltaTracker) {}

	public static boolean isCapturingCamera() {
		return false;
	}

	public static boolean amIBeingCaptured(BlockEntity be) {
		return false;
	}

	public static boolean shouldAddChunk(ChunkPos pos, int renderDistance) {
		return false;
	}

	public static boolean hasFeed(GlobalPos pos) {
		return false;
	}

	public static CameraFeed getFeed(GlobalPos pos) {
		return new CameraFeed();
	}

	public static void removeAllFrameLinks(GlobalPos pos) {}

	public static void removeAllFeeds() {}

	public static int getFrameFeedViewDistance(Object frame) {
		return 4;
	}

	public static class CameraFeed {
		public void requestFrustumUpdate() {}
	}
}
