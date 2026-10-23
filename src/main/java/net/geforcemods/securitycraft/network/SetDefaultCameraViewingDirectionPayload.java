package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetDefaultCameraViewingDirectionPayload(int id, float initialXRotation, float initialYRotation, float initialZoom) implements CustomPacketPayload {
	public static final Type<SetDefaultCameraViewingDirectionPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "set_default_camera_viewing_direction"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetDefaultCameraViewingDirectionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SetDefaultCameraViewingDirectionPayload::id,
			ByteBufCodecs.FLOAT, SetDefaultCameraViewingDirectionPayload::initialXRotation,
			ByteBufCodecs.FLOAT, SetDefaultCameraViewingDirectionPayload::initialYRotation,
			ByteBufCodecs.FLOAT, SetDefaultCameraViewingDirectionPayload::initialZoom,
			SetDefaultCameraViewingDirectionPayload::new);

	public SetDefaultCameraViewingDirectionPayload(SecurityCamera camera) {
		this(camera.getId(), camera.getXRot(), camera.getYRot(), camera.getZoomAmount());
	}

	public float xRot() {
		return initialXRotation;
	}

	public float yRot() {
		return initialYRotation;
	}

	public float zoom() {
		return initialZoom;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
