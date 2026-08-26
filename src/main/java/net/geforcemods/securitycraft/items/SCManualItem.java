package net.geforcemods.securitycraft.items;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.misc.SCManualPage;
import net.geforcemods.securitycraft.SecurityCraftClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** 1:1 with the upstream class of the same name. */
public class SCManualItem extends Item {
	public static final List<SCManualPage> PAGES = new ArrayList<>();

	public SCManualItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		//the screen is opened through the client entrypoint, like every other screen in this port: naming a client
		//class here directly makes Fabric refuse to load this item on a dedicated server
		if (level.isClientSide)
			SecurityCraftClient.openManualScreen();

		return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}
}
