package net.geforcemods.securitycraft.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record SetStateOnDisguiseModulePayload(BlockState state, StandingOrWallType standingOrWall) {
	public static final ResourceLocation CHANNEL = SCContent.id("set_state_on_disguise_module");

	public static SetStateOnDisguiseModulePayload read(FriendlyByteBuf buf) {
		return new SetStateOnDisguiseModulePayload(Block.stateById(buf.readVarInt()), buf.readEnum(StandingOrWallType.class));
	}

	public FriendlyByteBuf write() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(Block.getId(state));
		buf.writeEnum(standingOrWall);
		return buf;
	}

	public void handle(ServerPlayer player) {
		ItemStack stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.DISGUISE_MODULE);

		if (!stack.isEmpty()) {
			CompoundTag tag = stack.getOrCreateTag();

			tag.put("SavedState", NbtUtils.writeBlockState(state));
			tag.putInt("StandingOrWall", standingOrWall.ordinal());
		}
	}
}
