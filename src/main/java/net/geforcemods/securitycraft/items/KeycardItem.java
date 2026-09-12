package net.geforcemods.securitycraft.items;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A keycard: linked to one Keycard Reader (or Keycard Lock) network by matching signature + owner, then usable by
 * anyone the reader allows (everyone, unless a specific player name was set when linking). All keycard data lives in
 * the stack's NBT - the port has no data components on 1.20.1.
 * <p>
 * Simplified versus upstream: there the "limited" flag is set onto any keycard by crafting it together with a
 * Limited Use Keycard. Here the Limited Use Keycard is instead its own always-limited, level-1 item, to avoid an
 * NBT-copying custom recipe serializer.
 */
public class KeycardItem extends Item {
	private static final String TAG = "securitycraft:keycard";
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
		return stack.hasTag() && stack.getTag().contains(TAG);
	}

	private static CompoundTag data(ItemStack stack) {
		return stack.getTagElement(TAG);
	}

	public static int getSignature(ItemStack stack) {
		CompoundTag tag = data(stack);

		return tag == null ? 0 : tag.getInt("signature");
	}

	public static Optional<String> getUsableBy(ItemStack stack) {
		CompoundTag tag = data(stack);
		String usableBy = tag == null ? "" : tag.getString("usableBy");

		return usableBy.isEmpty() ? Optional.empty() : Optional.of(usableBy);
	}

	public static int getUsesLeft(ItemStack stack) {
		CompoundTag tag = data(stack);

		return tag == null ? 0 : tag.getInt("usesLeft");
	}

	public static void setUsesLeft(ItemStack stack, int usesLeft) {
		CompoundTag tag = stack.getOrCreateTagElement(TAG);

		tag.putInt("usesLeft", Math.max(0, usesLeft));
	}

	public static String getOwnerName(ItemStack stack) {
		CompoundTag tag = data(stack);

		return tag == null ? "" : tag.getString("ownerName");
	}

	public static String getOwnerUUID(ItemStack stack) {
		CompoundTag tag = data(stack);

		return tag == null ? "" : tag.getString("ownerUUID");
	}

	/** Links this keycard to a reader: its owner, signature and (optionally) the one player name allowed to use it. */
	public static void link(ItemStack stack, int signature, Optional<String> usableBy, String ownerName, String ownerUUID) {
		CompoundTag tag = stack.getOrCreateTagElement(TAG);

		tag.putInt("signature", signature);
		tag.putString("usableBy", usableBy.orElse(""));
		tag.putString("ownerName", ownerName);
		tag.putString("ownerUUID", ownerUUID);

		if (stack.getItem() instanceof KeycardItem keycard && keycard.limited && !tag.contains("usesLeft"))
			tag.putInt("usesLeft", 10);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
		if (!isLinked(stack)) {
			list.add(Component.translatable("tooltip.securitycraft:keycard.link_info").withStyle(ChatFormatting.GRAY));
			return;
		}

		list.add(Component.translatable("tooltip.securitycraft:keycard.signature", String.format("%05d", getSignature(stack))).withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("tooltip.securitycraft:keycard.usable_by", getUsableBy(stack).<Component>map(Component::literal).orElse(Component.translatable("tooltip.securitycraft:keycard.everyone"))).withStyle(ChatFormatting.GRAY));

		if (limited)
			list.add(Component.translatable("tooltip.securitycraft:keycard.uses", getUsesLeft(stack)).withStyle(ChatFormatting.GRAY));
	}
}
