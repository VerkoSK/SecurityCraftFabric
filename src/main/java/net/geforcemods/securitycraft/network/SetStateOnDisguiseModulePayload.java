package net.geforcemods.securitycraft.network;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SCStreamCodecs;
import net.geforcemods.securitycraft.components.SavedBlockState;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public record SetStateOnDisguiseModulePayload(BlockState state, StandingOrWallType standingOrWall) implements CustomPacketPayload {
	public static final Type<SetStateOnDisguiseModulePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("securitycraft", "set_state_on_disguise_module"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetStateOnDisguiseModulePayload> CODEC = StreamCodec.composite(
			SCStreamCodecs.BLOCK_STATE, SetStateOnDisguiseModulePayload::state,
			ByteBufCodecs.idMapper(i -> StandingOrWallType.values()[i], StandingOrWallType::ordinal), SetStateOnDisguiseModulePayload::standingOrWall,
			SetStateOnDisguiseModulePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public StandingOrWallType standingOrWallType() {
		return standingOrWall;
	}

	public void handle(Player player) {
		ItemStack stack = PlayerUtils.getItemStackFromAnyHand(player, SCContent.DISGUISE_MODULE);

		if (!player.isSpectator() && !stack.isEmpty()) {
			if (state.isAir())
				stack.set(SCContent.SAVED_BLOCK_STATE, SavedBlockState.EMPTY);
			else
				stack.set(SCContent.SAVED_BLOCK_STATE, new SavedBlockState(state, standingOrWall));
		}
	}
}
