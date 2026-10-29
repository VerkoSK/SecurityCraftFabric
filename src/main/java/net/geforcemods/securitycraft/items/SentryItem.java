package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class SentryItem extends Item {
	public SentryItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Direction facing = ctx.getClickedFace();
		ItemStack stack = ctx.getItemInHand();
		boolean replacesTargetedBlock = state.canBeReplaced(new BlockPlaceContext(ctx));

		if (!replacesTargetedBlock) {
			pos = pos.relative(facing); //if the block is not replaceable, place sentry next to targeted block
			state = level.getBlockState(pos);
		}

		if (!level.isEmptyBlock(pos) && !replacesTargetedBlock)
			return InteractionResult.PASS;
		else {
			BlockPos downPos = pos.below();

			if (level.isEmptyBlock(downPos) || level.noCollision(new AABB(downPos))) {
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY.getDescriptionId()), Utils.localize("messages.securitycraft:sentry.needsBlockBelow"), ChatFormatting.DARK_RED);
				return InteractionResult.FAIL;
			}
		}

		Sentry entity = SCContent.SENTRY_ENTITY.create(level);

		entity.setupSentry(player);
		entity.setPos(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);

		if (stack.hasCustomHoverName())
			entity.setCustomName(stack.getHoverName());

		if (replacesTargetedBlock)
			level.destroyBlock(pos, false);

		level.addFreshEntity(entity);
		level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);

		if (!player.isCreative())
			stack.shrink(1);

		return InteractionResult.SUCCESS;
	}
}
