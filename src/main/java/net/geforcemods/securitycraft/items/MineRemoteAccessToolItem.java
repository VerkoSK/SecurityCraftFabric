package net.geforcemods.securitycraft.items;

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
import net.minecraft.core.GlobalPos;
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

/** The mine remote access tool. 1:1 with the upstream class of the same name. */
public class MineRemoteAccessToolItem extends Item {
	public MineRemoteAccessToolItem(Item.Properties properties) {
		super(properties);
	}

	/**
	 * Fabric stand-in for NeoForge's {@code IItemExtension#onItemUseFirst}: {@link UseBlockCallback} runs before the clicked
	 * block's own use handling, which is what makes binding work on mines that open a GUI (furnace mine, redstone ore mine).
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
			BoundMines boundMines = stack.get(SCContent.BOUND_MINES);

			if (boundMines == null)
				return InteractionResult.PASS;

			GlobalPos globalPos = new GlobalPos(level.dimension(), pos);

			//UseBlockCallback runs on both sides. Letting the client edit its own copy of the stack as well makes the
			//two sides drift apart - each toggles independently, so a bind on one can line up with an unbind on the
			//other - so only the server touches the component and the client just consumes the interaction. Returning
			//SUCCESS still sends the packet: Fabric's client mixin only swallows it for CONSUME and FAIL.
			if (level.isClientSide) {
				if (!boundMines.contains(globalPos) && boundMines.firstFreeSlot() != -1 && level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(player))
					SecurityCraftClient.openMRATScreen(stack);

				return InteractionResult.SUCCESS;
			}

			int boundSlot = boundMines.indexOf(globalPos);

			if (boundSlot == -1) {
				int freeSlot = boundMines.firstFreeSlot();

				if (freeSlot == -1) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.noSlots"), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}

				if (level.getBlockEntity(pos) instanceof IOwnable ownable && !ownable.isOwnedBy(player))
					return InteractionResult.SUCCESS;

				stack.set(SCContent.BOUND_MINES, boundMines.with(freeSlot, globalPos));
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.bound", Utils.getFormattedCoordinates(pos)), ChatFormatting.GREEN);
				return InteractionResult.SUCCESS;
			}
			else {
				stack.set(SCContent.BOUND_MINES, boundMines.with(boundSlot, null));
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.MINE_REMOTE_ACCESS_TOOL.getDescriptionId()), Utils.localize("messages.securitycraft:mrat.unbound", Utils.getFormattedCoordinates(pos)), ChatFormatting.RED);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
		BoundMines boundMines = stack.get(SCContent.BOUND_MINES);

		if (boundMines == null || !display.shows(SCContent.BOUND_MINES))
			return;

		for (int i = 0; i < BoundMines.MAX_MINES; i++) {
			GlobalPos globalPos = boundMines.get(i);

			if (globalPos == null)
				tooltipAdder.accept(Component.literal(ChatFormatting.GRAY + "---"));
			else
				tooltipAdder.accept(Utils.localize("tooltip.securitycraft:mine", i + 1, Utils.getFormattedCoordinates(globalPos.pos())).setStyle(Utils.GRAY_STYLE));
		}
	}
}
