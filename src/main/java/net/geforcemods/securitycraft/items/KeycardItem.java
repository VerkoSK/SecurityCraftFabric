package net.geforcemods.securitycraft.items;

import java.util.List;
import java.util.Optional;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.KeycardData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * A keycard: linked to one Keycard Reader (or Keycard Lock) network by matching signature + owner, then usable by
 * anyone the reader allows (everyone, unless a specific player name was set when linking). On 1.20.1 all keycard data
 * lives in the stack's NBT; this port has no NBT tags, so it lives in the {@link KeycardData} data component instead
 * (see {@code SCContent#KEYCARD_DATA}).
 * <p>
 * Simplified versus upstream: there the "limited" flag is set onto any keycard by crafting it together with a
 * Limited Use Keycard. Here the Limited Use Keycard is instead its own always-limited, level-1 item, to avoid a
 * component-copying custom recipe serializer.
 */
public class KeycardItem extends Item {
	private final int level;
	private final boolean limited;

	public KeycardItem(Properties properties, int level) {
		this(properties, level, false);
	}

	public KeycardItem(Properties properties, int level, boolean limited) {
		super(properties);
		this.level = level;
		this.limited = limited;
	}

	/** 0-indexed: the level 1 keycard returns 0, the level 5 keycard returns 4. */
	public int getLevel() {
		return level;
	}

	public boolean isLimited() {
		return limited;
	}

	public static boolean isLinked(ItemStack stack) {
		return stack.has(SCContent.KEYCARD_DATA);
	}

	private static KeycardData data(ItemStack stack) {
		return stack.getOrDefault(SCContent.KEYCARD_DATA, KeycardData.EMPTY);
	}

	public static int getSignature(ItemStack stack) {
		return data(stack).signature();
	}

	public static Optional<String> getUsableBy(ItemStack stack) {
		return data(stack).usableByOptional();
	}

	public static int getUsesLeft(ItemStack stack) {
		return data(stack).usesLeft();
	}

	public static void setUsesLeft(ItemStack stack, int usesLeft) {
		KeycardData current = data(stack);

		stack.set(SCContent.KEYCARD_DATA, new KeycardData(current.signature(), current.usableBy(), current.ownerName(), current.ownerUUID(), Math.max(0, usesLeft)));
	}

	public static String getOwnerName(ItemStack stack) {
		return data(stack).ownerName();
	}

	public static String getOwnerUUID(ItemStack stack) {
		return data(stack).ownerUUID();
	}

	/** Links this keycard to a reader: its owner, signature and (optionally) the one player name allowed to use it. */
	public static void link(ItemStack stack, int signature, Optional<String> usableBy, String ownerName, String ownerUUID) {
		KeycardData current = data(stack);
		boolean limited = stack.getItem() instanceof KeycardItem keycard && keycard.limited;
		int usesLeft = limited ? (isLinked(stack) ? current.usesLeft() : 10) : 0;

		stack.set(SCContent.KEYCARD_DATA, new KeycardData(signature, usableBy.orElse(""), ownerName, ownerUUID, usesLeft));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
		if (!isLinked(stack)) {
			tooltip.accept(Component.translatable("tooltip.securitycraft:keycard.link_info").withStyle(ChatFormatting.GRAY));
			return;
		}

		tooltip.accept(Component.translatable("tooltip.securitycraft:keycard.signature", String.format("%05d", getSignature(stack))).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("tooltip.securitycraft:keycard.usable_by", getUsableBy(stack).<Component>map(Component::literal).orElse(Component.translatable("tooltip.securitycraft:keycard.everyone"))).withStyle(ChatFormatting.GRAY));

		if (limited)
			tooltip.accept(Component.translatable("tooltip.securitycraft:keycard.uses", getUsesLeft(stack)).withStyle(ChatFormatting.GRAY));
	}
}
