package net.geforcemods.securitycraft.compat.jade;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows a block's owner — and its installed modules and custom name — in Jade's tooltip, the way the original
 * mod does. Jade is optional: this class is only ever loaded when Jade is present.
 */
@WailaPlugin(SecurityCraft.MODID)
public final class SCJadePlugin implements IWailaPlugin, IBlockComponentProvider {
	private static final Identifier ID = Identifier.fromNamespaceAndPath(SecurityCraft.MODID, "info");
	private static final Identifier SHOW_OWNER = Identifier.fromNamespaceAndPath(SecurityCraft.MODID, "showowner");
	private static final Identifier SHOW_MODULES = Identifier.fromNamespaceAndPath(SecurityCraft.MODID, "showmodules");
	private static final Identifier SHOW_CUSTOM_NAME = Identifier.fromNamespaceAndPath(SecurityCraft.MODID, "showcustomname");

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(SHOW_OWNER, true);
		registration.addConfig(SHOW_MODULES, true);
		registration.addConfig(SHOW_CUSTOM_NAME, true);
		registration.registerBlockComponent(this, Block.class);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockEntity be = accessor.getBlockEntity();

		if (be == null)
			return;

		if (be instanceof IOwnable ownable && config.get(SHOW_OWNER))
			tooltip.add(Utils.localize("waila.securitycraft:owner", PlayerUtils.getOwnerComponent(ownable.getOwner())).withStyle(ChatFormatting.GRAY));

		//an ownable block only lists its modules to its owner, everything else always lists them
		if (be instanceof IModuleInventory inv && config.get(SHOW_MODULES) && !inv.getInsertedModules().isEmpty() && (!(be instanceof IOwnable ownable) || ownable.isOwnedBy(accessor.getPlayer()))) {
			tooltip.add(Utils.localize("waila.securitycraft:equipped").withStyle(ChatFormatting.GRAY));

			for (ModuleType module : inv.getInsertedModules()) {
				MutableComponent prefix;

				if (inv.isModuleEnabled(module))
					prefix = Component.literal("✔ ").withStyle(ChatFormatting.GREEN);
				else
					prefix = Component.literal("✕ ").withStyle(ChatFormatting.RED);

				tooltip.add(prefix.append(Component.translatable(module.getTranslationKey()).withStyle(ChatFormatting.GRAY)));
			}
		}

		if (config.get(SHOW_CUSTOM_NAME) && be instanceof Nameable nameable && nameable.hasCustomName()) {
			Component name = nameable.getCustomName();

			tooltip.add(Utils.localize("waila.securitycraft:customName", name == null ? Component.empty() : name).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public Identifier getUid() {
		return ID;
	}
}
