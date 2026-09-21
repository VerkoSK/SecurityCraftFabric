package net.geforcemods.securitycraft.items;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.KeycardData;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * A keycard: linked to one Keycard Reader (or Keycard Lock) network by matching signature + owner, then usable by
 * anyone the reader allows (everyone, unless a specific player name was set when linking). Crafting a keycard together
 * with a Limited Use Keycard makes it limited-use, 1:1 with upstream. On 1.20.1 all of this lives directly in the
 * stack's NBT; this port has no NBT tags, so it lives in the {@link KeycardData} data component instead (see
 * {@code SCContent#KEYCARD_DATA}).
 */
public class KeycardItem extends Item {
	private static final Component LINK_INFO = Component.translatable("tooltip.securitycraft:keycard.link_info").setStyle(Utils.GRAY_STYLE);
	public static final Component LIMITED_INFO = Component.translatable("tooltip.securitycraft:keycard.limited_info").setStyle(Utils.GRAY_STYLE);
	public static final Component LIMITED_INFO_1 = Component.translatable("tooltip.securitycraft:keycard.limited_info_1").setStyle(Utils.GRAY_STYLE);
	public static final Component LIMITED_INFO_2 = Component.translatable("tooltip.securitycraft:keycard.limited_info_2").setStyle(Utils.GRAY_STYLE);
	private final int level; //0-indexed

	public KeycardItem(Item.Properties properties, int level) {
		super(properties);
		this.level = level;
	}

	/**
	 * @return 0-indexed level of this keycard. Example: The level 1 keycard will return 0, and the level 5 keycard will return 4
	 */
	public int getLevel() {
		return level;
	}

	private static KeycardData data(ItemStack stack) {
		return stack.getOrDefault(SCContent.KEYCARD_DATA, KeycardData.EMPTY);
	}

	public static boolean isLinked(ItemStack stack) {
		return data(stack).linked();
	}

	public static boolean isLimited(ItemStack stack) {
		return data(stack).limited();
	}

	public static int getSignature(ItemStack stack) {
		return data(stack).signature();
	}

	public static String getOwnerName(ItemStack stack) {
		return data(stack).ownerName();
	}

	public static String getOwnerUUID(ItemStack stack) {
		return data(stack).ownerUUID();
	}

	public static Optional<String> getUsableBy(ItemStack stack) {
		return data(stack).usableByOptional();
	}

	public static int getUsesLeft(ItemStack stack) {
		return data(stack).usesLeft();
	}

	public static void setUsesLeft(ItemStack stack, int usesLeft) {
		stack.set(SCContent.KEYCARD_DATA, data(stack).withUsesLeft(usesLeft));
	}

	public static void link(ItemStack stack, int signature, Optional<String> usableBy, String ownerName, String ownerUUID) {
		stack.set(SCContent.KEYCARD_DATA, data(stack).linkedWith(signature, usableBy, ownerName, ownerUUID));
	}

	/** Makes {@code stack} limited-use, keeping its existing link data if any. Used by {@code LimitedUseKeycardRecipe}. */
	public static KeycardData madeLimited(ItemStack stack) {
		return data(stack).madeLimited();
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		if (this == SCContent.LIMITED_USE_KEYCARD)
			return;

		KeycardData data = data(stack);

		if (data.linked()) {
			list.add(Component.translatable("tooltip.securitycraft:keycard.signature", StringUtils.leftPad("" + data.signature(), 5, "0")).setStyle(Utils.GRAY_STYLE));
			list.add(Component.translatable("tooltip.securitycraft:keycard.reader_owner", data.ownerName()).setStyle(Utils.GRAY_STYLE));
			list.add(Component.translatable("tooltip.securitycraft:keycard.usable_by", data.usableByOptional().<Component>map(Component::literal).orElse(Component.translatable("tooltip.securitycraft:keycard.everyone"))).setStyle(Utils.GRAY_STYLE));
		}
		else {
			list.add(Component.translatable("tooltip.securitycraft:keycard.reader_owner", data.ownerName()).setStyle(Utils.GRAY_STYLE));
			list.add(LINK_INFO);
		}

		if (data.limited())
			list.add(Component.translatable("tooltip.securitycraft:keycard.uses", data.usesLeft()).setStyle(Utils.GRAY_STYLE));
		else {
			list.add(LIMITED_INFO_1);
			list.add(LIMITED_INFO_2);
		}
	}
}
