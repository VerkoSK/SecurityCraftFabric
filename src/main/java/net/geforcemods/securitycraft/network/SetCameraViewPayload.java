package net.geforcemods.securitycraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SetCameraViewPayload(int entityId) implements CustomPacketPayload {
	public static final Type<SetCameraViewPayload> TYPE = new Type<>(new ResourceLocation("securitycraft", "set_camera_view"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetCameraViewPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SetCameraViewPayload::entityId,
			SetCameraViewPayload::new);

	public SetCameraViewPayload(Entity camera) {
		this(camera.getId());
	}

	public int id() {
		return entityId;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
