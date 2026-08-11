package net.geforcemods.securitycraft.items;

import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.components.BoundMines;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** The mine remote access tool. 1:1 with the upstream class of the same name, storing bound mines in {@link BoundMines} instead of upstream's {@code GlobalPositions}. */
public class MineRemoteAccessToolItem extends Item {
	public MineRemoteAccessToolItem(Item.Properties properties) {
		super(properties);
	}

	/**
	 * Fabric stand-in for NeoForge's {@code onItemUseFirst}: {@link UseBlockCallback} runs before the clicked block's
	 * {@code use}, which is what makes binding work on mines that open a GUI (furnace mine, redstone ore mine).
	 * Must be called from the mod initializer, or the callback is never registered and nothing binds at all.
	 */
	public static void registerBindingCallback() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (stack.getItem() instanceof MineRemoteAccessToolItem mrat)
				return mrat.onItemUseFirst(stack, new UseOnContext(player, hand, hitResult));

			return InteractionResult.PASS;
		});
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide)
			SecurityCraftClient.openMRATScreen(player.getItemInHand(hand));

		return InteractionResult.CONSUME;
	}

	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();

		if (level.getBlockState(pos).getBlock() instanceof IExplosive) {
			Player player = ctx.getPlayer();
			BoundMines mines = stack.getOrDefault(SCContent.BOUND_MINES, BoundMines.EMPTY);

			//UseBlockCallback runs on both sides. Letting the client edit its own copy of the stack as well makes the
			//two sides drift apart - each toggles independently, so a bind on one can line up with an unbind on the
			//other - so only the server touches the component and the client just consumes the interaction. Returning
			//SUCCESS still sends the packet: Fabric's client mixin only swallows it for CONSUME and FAIL.
			if (level.isClientSide) {
				if (!mines.contains(pos) && mines.getNextAvailableSlot() != -1 && level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(player))
					SecurityCraftClient.openMRATScreen(stack);

				return InteractionResult.SUCCESS;
			}

			if (!mines.contains(pos)) {
				if (mines.getNextAvailableSlot() == -1) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.noSlots"), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}

				if (level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(player))
					return InteractionResult.SUCCESS;

				stack.set(SCContent.BOUND_MINES, mines.with(pos));
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.bound", Utils.getFormattedCoordinates(pos)), ChatFormatting.GREEN);
				return InteractionResult.SUCCESS;
			}
			else {
				stack.set(SCContent.BOUND_MINES, mines.without(pos));
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.unbound", Utils.getFormattedCoordinates(pos)), ChatFormatting.RED);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
		BoundMines mines = stack.get(SCContent.BOUND_MINES);

		if (mines == null)
			return;

		List<BlockPos> positions = mines.positions();

		for (int i = 0; i < positions.size(); i++) {
			BlockPos pos = positions.get(i);

			if (pos == null)
				tooltipAdder.accept(Component.literal(ChatFormatting.GRAY + "---"));
			else
				tooltipAdder.accept(Utils.localize("tooltip.securitycraft:mine", i + 1, Utils.getFormattedCoordinates(pos)).setStyle(Utils.GRAY_STYLE));
		}
	}

	public static boolean hasMineAdded(ItemStack stack) {
		BoundMines mines = stack.get(SCContent.BOUND_MINES);

		return mines != null && !mines.isEmpty();
	}
}
