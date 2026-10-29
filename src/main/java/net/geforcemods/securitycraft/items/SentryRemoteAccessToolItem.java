package net.geforcemods.securitycraft.items;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SentryRemoteAccessToolItem extends Item {
	public SentryRemoteAccessToolItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			updateDynamicNames(level, stack);
			NetworkHandler.openSRATScreen((ServerPlayer) player);
		}

		return InteractionResultHolder.consume(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		List<Sentry> sentries = level.getEntitiesOfClass(Sentry.class, new AABB(pos));
		ItemStack stack = ctx.getItemInHand();

		if (!sentries.isEmpty()) {
			Sentry sentry = sentries.get(0);
			BlockPos sentryPos = sentry.blockPosition();

			if (!sentry.isOwnedBy(player)) {
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.getDescriptionId()), Utils.localize("messages.securitycraft:srat.cantBind"), ChatFormatting.RED);
				return InteractionResult.FAIL;
			}

			if (stack.getTag() == null)
				stack.setTag(new CompoundTag());

			if (!isSentryAdded(stack, sentryPos)) {
				int availSlot = getNextAvailableSlot(stack);

				if (availSlot == 0) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.getDescriptionId()), Utils.localize("messages.securitycraft:srat.noSlots"), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}

				stack.getTag().putIntArray("sentry" + availSlot, new int[] {
						sentryPos.getX(), sentryPos.getY(), sentryPos.getZ()
				});

				if (sentry.hasCustomName())
					stack.getTag().putString("sentry" + availSlot + "_name", sentry.getCustomName().getString());

				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.getDescriptionId()), Utils.localize("messages.securitycraft:srat.bound", sentryPos), ChatFormatting.GREEN);
			}
			else {
				removeTagFromItemAndUpdate(stack, sentryPos);
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.getDescriptionId()), Utils.localize("messages.securitycraft:srat.unbound", sentryPos), ChatFormatting.RED);
			}

			return InteractionResult.SUCCESS;
		}
		else if (!level.isClientSide) {
			updateDynamicNames(level, stack);
			NetworkHandler.openSRATScreen((ServerPlayer) player);
		}

		return InteractionResult.SUCCESS;
	}

	private void updateDynamicNames(Level level, ItemStack stack) {
		if (!stack.hasTag())
			return;

		CompoundTag tag = stack.getTag();

		for (int i = 1; i <= 12; i++) {
			if (!tag.contains("sentry" + i))
				continue;

			int[] coords = tag.getIntArray("sentry" + i);
			BlockPos sentryPos = new BlockPos(coords[0], coords[1], coords[2]);

			if (level.isLoaded(sentryPos)) {
				List<Sentry> sentries = level.getEntitiesOfClass(Sentry.class, new AABB(sentryPos));

				if (!sentries.isEmpty()) {
					Sentry sentry = sentries.get(0);

					if (sentry.hasCustomName())
						tag.putString("sentry" + i + "_name", sentry.getCustomName().getString());
					else
						tag.remove("sentry" + i + "_name");
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length != 3)
				continue;
			else {
				String nameKey = "sentry" + i + "_name";
				MutableComponent text = Component.literal(ChatFormatting.GRAY + "--- #" + i + ": ");

				if (stack.getTag().contains(nameKey))
					text.append(stack.getTag().getString(nameKey) + " ");

				tooltip.add(text.append(Utils.getFormattedCoordinates(new BlockPos(coords[0], coords[1], coords[2])).getString()).withStyle(Utils.GRAY_STYLE));
			}
		}
	}

	private void removeTagFromItemAndUpdate(ItemStack stack, BlockPos pos) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length == 3 && coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ()) {
				stack.getTag().remove("sentry" + i);
				stack.getTag().remove("sentry" + i + "_name");
				return;
			}
		}
	}

	public static boolean isSentryAdded(ItemStack stack, BlockPos pos) {
		if (stack.getTag() == null)
			return false;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length == 3 && coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ())
				return true;
		}

		return false;
	}

	public static int getNextAvailableSlot(ItemStack stack) {
		if (stack.getTag() == null)
			return 1;

		for (int i = 1; i <= 12; i++) {
			if (stack.getTag().getIntArray("sentry" + i).length != 3)
				return i;
		}

		return 0;
	}

	public static boolean hasSentryAdded(CompoundTag tag) {
		if (tag == null)
			return false;

		for (int i = 1; i <= 12; i++) {
			if (tag.contains("sentry" + i))
				return true;
		}

		return false;
	}
}
