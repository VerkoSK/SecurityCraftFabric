package net.geforcemods.securitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.DetectionMode;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place", at = @At("RETURN"))
	private void securitycraft$onBlockPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (cir.getReturnValue().consumesAction()) {
			Player player = context.getPlayer();

			if (player != null) {
				Level level = context.getLevel();

				if (!level.isClientSide()) {
					BlockPos pos = context.getClickedPos();
					BlockState state = level.getBlockState(pos);

					BlockEntityTracker.BLOCK_CHANGE_DETECTOR.getBlockEntitiesInRange(level, pos).forEach(detector -> detector.log(player, DetectionMode.PLACE, pos, state));
				}
			}
		}
	}
}
