package net.geforcemods.securitycraft.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class SecureTradingStationRenderState extends BlockEntityRenderState {
	public boolean disguised;
	public Direction facing;
	public final ItemStackRenderState paymentDisplay = new ItemStackRenderState();
	public final ItemStackRenderState rewardDisplay = new ItemStackRenderState();
	public boolean hasPayment;
	public boolean hasReward;
}
