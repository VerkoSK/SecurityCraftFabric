package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Converts a vanilla block into its reinforced counterpart on right-click. */
public class BlockReinforcerItem extends Item {
	public BlockReinforcerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());

		if (!"minecraft".equals(key.getNamespace()))
			return InteractionResult.PASS;

		Block reinforced = SCContent.REINFORCED_BY_NAME.get("reinforced_" + key.getPath());

		if (reinforced == null)
			return InteractionResult.PASS;

		if (level instanceof ServerLevel)
			level.setBlockAndUpdate(pos, reinforced.withPropertiesOf(state));

		return InteractionResult.SUCCESS;
	}
}
