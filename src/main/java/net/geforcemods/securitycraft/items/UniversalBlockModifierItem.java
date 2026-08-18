package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The Universal Block Modifier. Upstream opens the customize-block screen with it, which lets the owner change a
 * block's options; that screen is not ported yet, so for now it reports who owns the block instead. It is also
 * the ingredient the Universal Owner Changer is crafted from.
 */
public class UniversalBlockModifierItem extends Item {
	public UniversalBlockModifierItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getLevel().isClientSide)
			return InteractionResult.SUCCESS;

		if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof IOwnable ownable))
			return InteractionResult.PASS;

		PlayerUtils.sendMessageToPlayer(ctx.getPlayer(), Utils.localize(SCContent.UNIVERSAL_BLOCK_MODIFIER.getDescriptionId()), Utils.localize("messages.securitycraft:universalBlockModifier.owner", ownable.getOwner().getName()), ChatFormatting.GREEN);
		return InteractionResult.SUCCESS;
	}
}
