package net.geforcemods.securitycraft.util;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityRenderDelegate {
	private final Map<BlockEntity, DelegateRendererInfo> renderDelegates = new HashMap<>();

	public void putDisguisedTeRenderer(BlockEntity disguisableBlockEntity, ItemStack stack) {
		if (!stack.hasTag())
			return;

		Level level = Minecraft.getInstance().level;

		putDelegateFor(disguisableBlockEntity, NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), stack.getOrCreateTag().getCompound("SavedState")));
	}

	public void putDelegateFor(BlockEntity originalBlockEntity, BlockState delegateState) {
		if (renderDelegates.containsKey(originalBlockEntity)) {
			DelegateRendererInfo delegateInfo = renderDelegates.get(originalBlockEntity);

			//the original be already has a delegate block entity of the same type, just update the state instead of creating a whole new be and renderer
			if (delegateInfo.delegateBlockEntity.getBlockState().getBlock() == delegateState.getBlock()) {
				delegateInfo.delegateBlockEntity.setBlockState(delegateState);
				return;
			}
		}

		if (delegateState != null && delegateState.hasBlockEntity()) {
			Minecraft mc = Minecraft.getInstance();
			BlockEntity delegateBe = ((EntityBlock) delegateState.getBlock()).newBlockEntity(BlockPos.ZERO, delegateState);

			if (delegateBe != null) {
				BlockEntityRenderer<?> delegateBeRenderer;

				delegateBe.setLevel(mc.level);
				delegateBeRenderer = mc.getBlockEntityRenderDispatcher().getRenderer(delegateBe);

				if (delegateBeRenderer != null)
					renderDelegates.put(originalBlockEntity, new DelegateRendererInfo(delegateBe, delegateBeRenderer));
			}
		}
	}

	public void removeDelegateOf(BlockEntity originalBlockEntity) {
		renderDelegates.remove(originalBlockEntity);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	public boolean tryRenderDelegate(BlockEntity originalBlockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		DelegateRendererInfo delegateRendererInfo = renderDelegates.get(originalBlockEntity);

		if (delegateRendererInfo != null) {
			try {
				PoseStack copyPose = new PoseStack();

				copyPose.pushPose();
				copyPose.last().pose().mul(pose.last().pose());
				copyPose.last().normal().mul(pose.last().normal());
				((BlockEntityRenderer<BlockEntity>) delegateRendererInfo.delegateRenderer()).render(delegateRendererInfo.delegateBlockEntity(), partialTicks, copyPose, buffer, combinedLight, combinedOverlay);
				copyPose.popPose();
			}
			catch (Exception e) {
				SecurityCraft.LOGGER.warn("Error when delegate-rendering {}", BuiltInRegistriesHelper.getKey(delegateRendererInfo.delegateBlockEntity().getType()));
				e.printStackTrace();
				removeDelegateOf(originalBlockEntity);
			}

			return true;
		}

		return false;
	}

	private static final class BuiltInRegistriesHelper {
		static Object getKey( net.minecraft.world.level.block.entity.BlockEntityType<?> type) {
			return net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
		}
	}

	private static record DelegateRendererInfo(BlockEntity delegateBlockEntity, BlockEntityRenderer<?> delegateRenderer) {}
}
