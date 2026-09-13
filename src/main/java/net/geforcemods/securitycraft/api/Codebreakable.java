package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.CodebreakerItem;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Marks a block entity as hackable with the Codebreaker. Ported from upstream's {@code ICodebreakable}; the port has no
 * data components, so the per-use cooldown and success flag live in the codebreaker stack's NBT (see
 * {@link CodebreakerItem}).
 */
public interface Codebreakable {
	/** Checked before any attempt: return false to abort silently (or after sending your own message). */
	boolean shouldAttemptCodebreak(Player player);

	/** Called once the code has been successfully broken. */
	void useCodebreaker(Player player);

	/**
	 * Runs a full codebreaking attempt: owner/creative checks, cooldown, the random roll, durability damage and player
	 * feedback.
	 *
	 * @return true if the code was broken
	 */
	default boolean handleCodebreaking(Player player, InteractionHand hand) {
		ItemStack codebreaker = player.getItemInHand(hand);

		if (!codebreaker.is(SCContent.CODEBREAKER))
			return false;

		if (!ConfigHandler.allowCodebreakerItem) {
			PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.CODEBREAKER.getDescriptionId()), Utils.localize("messages.securitycraft:codebreakerDisabled"), ChatFormatting.RED);
			return false;
		}

		if (!shouldAttemptCodebreak(player))
			return false;

		boolean canBypass = player.isCreative() || player.isSpectator();

		if (this instanceof IOwnable ownable && ownable.isOwnedBy(player) && !canBypass) {
			PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.CODEBREAKER.getDescriptionId()), Utils.localize("messages.securitycraft:codebreaker.owned"), ChatFormatting.RED);
			return false;
		}

		if (!canBypass && CodebreakerItem.wasRecentlyUsed(codebreaker))
			return false;

		boolean isSuccessful = player.getRandom().nextDouble() < ConfigHandler.codebreakerChance;

		if (!canBypass) {
			codebreaker.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
			CodebreakerItem.markUsed(codebreaker);
		}

		if (isSuccessful)
			useCodebreaker(player);
		else {
			PlayerUtils.sendMessageToPlayer(player, Component.translatable(SCContent.CODEBREAKER.getDescriptionId()), Utils.localize("messages.securitycraft:codebreaker.failed"), ChatFormatting.RED);
			return false;
		}

		return true;
	}
}
