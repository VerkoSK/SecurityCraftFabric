package net.geforcemods.securitycraft.items;

import java.util.List;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Codebreakable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
 * cooldown so it cannot be spammed. The port has no NBT tags on 1.21.1, so the last-used timestamp lives in the
 * {@link SCContent#CODEBREAKER_LAST_USED} data component instead of upstream's item NBT tag.
 */
public class CodebreakerItem extends Item {
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

			if (level.isClientSide())
				return InteractionResult.SUCCESS;

			codebreakable.handleCodebreaking(player, hand);
			return InteractionResult.SUCCESS;
		});
	}

	public static boolean wasRecentlyUsed(ItemStack codebreaker) {
		Long lastUsed = codebreaker.get(SCContent.CODEBREAKER_LAST_USED);

		return lastUsed != null && System.currentTimeMillis() - lastUsed < COOLDOWN_MS;
	}

	public static void markUsed(ItemStack codebreaker) {
		codebreaker.set(SCContent.CODEBREAKER_LAST_USED, System.currentTimeMillis());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
		if (!ConfigHandler.allowCodebreakerItem)
			tooltip.accept(Component.translatable("tooltip.securitycraft.codebreaker.disabled").withStyle(ChatFormatting.RED));
		else
			tooltip.accept(Component.translatable("tooltip.securitycraft.codebreaker.chance", (int) (ConfigHandler.codebreakerChance * 100) + "%").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

}
