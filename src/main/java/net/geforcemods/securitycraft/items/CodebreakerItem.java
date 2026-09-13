package net.geforcemods.securitycraft.items;

import java.util.List;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.api.Codebreakable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Attacker-side tool: right-click a passcode-protected block to try to break into it. Each attempt has a fixed success
 * chance ({@link ConfigHandler#codebreakerChance}), costs one durability point and puts the codebreaker on a short
 * cooldown so it cannot be spammed.
 */
public class CodebreakerItem extends Item {
	private static final String LAST_USED_TAG = "codebreaker_last_used";
	private static final long COOLDOWN_MS = 3000L;

	public CodebreakerItem(Item.Properties properties) {
		super(properties);
	}

	/** Fabric replacement for Forge's {@code onItemUseFirst}. */
	public static void registerUseCallback() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (!(player.getItemInHand(hand).getItem() instanceof CodebreakerItem))
				return InteractionResult.PASS;

			BlockPos pos = hitResult.getBlockPos();
			BlockState state = level.getBlockState(pos);

			if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
				pos = pos.below();

			if (!(level.getBlockEntity(pos) instanceof Codebreakable codebreakable))
				return InteractionResult.PASS;

			if (level.isClientSide)
				return InteractionResult.SUCCESS;

			codebreakable.handleCodebreaking(player, hand);
			return InteractionResult.SUCCESS;
		});
	}

	public static boolean wasRecentlyUsed(ItemStack codebreaker) {
		CompoundTag tag = codebreaker.getTag();

		return tag != null && tag.contains(LAST_USED_TAG) && System.currentTimeMillis() - tag.getLong(LAST_USED_TAG) < COOLDOWN_MS;
	}

	public static void markUsed(ItemStack codebreaker) {
		codebreaker.getOrCreateTag().putLong(LAST_USED_TAG, System.currentTimeMillis());
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		if (!ConfigHandler.allowCodebreakerItem)
			tooltip.add(Component.translatable("tooltip.securitycraft.codebreaker.disabled").withStyle(ChatFormatting.RED));
		else
			tooltip.add(Component.translatable("tooltip.securitycraft.codebreaker.chance", (int) (ConfigHandler.codebreakerChance * 100) + "%").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
}
