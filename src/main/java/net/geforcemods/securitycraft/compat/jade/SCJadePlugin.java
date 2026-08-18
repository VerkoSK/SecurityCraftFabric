package net.geforcemods.securitycraft.compat.jade;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
	private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, "info");
	private static final ResourceLocation SHOW_OWNER = ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, "show_owner");
	private static final ResourceLocation SHOW_MODULES = ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, "show_modules");
	private static final ResourceLocation SHOW_CUSTOM_NAME = ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, "show_custom_name");

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

		if (be instanceof IOwnable ownable && config.get(SHOW_OWNER)) {
			Owner owner = ownable.getOwner();

			tooltip.add(Utils.localize("waila.securitycraft:owner", owner.owns() || !owner.getName().isEmpty() ? owner.getName() : "????").withStyle(ChatFormatting.GRAY));
		}

		if (be instanceof IModuleInventory inv && config.get(SHOW_MODULES) && !inv.getInsertedModules().isEmpty()) {
			tooltip.add(Utils.localize("waila.securitycraft:modules").withStyle(ChatFormatting.GRAY));

			for (ModuleType module : inv.getInsertedModules()) {
				tooltip.add(Component.literal("- ").append(Utils.localize(module.getTranslationKey())).withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}
}
