package net.geforcemods.securitycraft.compat.jade;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.misc.ContainerLockData;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows a block's owner — and its installed modules and custom name — in Jade's tooltip, the way the original
 * mod does. Jade is optional: this class is only ever loaded when Jade is present.
 * <p>
 * Also flags a modded container the Key Panel locked via {@link ContainerLockData} (not a real SecurityCraft block,
 * so it carries no owner-implementing block entity Jade could read directly) - the lock lives in server-only save
 * data, so it needs its own {@link IServerDataProvider} to sync the "is this locked" flag to the client.
 */
@WailaPlugin(SecurityCraft.MODID)
public final class SCJadePlugin implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	private static final ResourceLocation ID = new ResourceLocation(SecurityCraft.MODID, "info");
	private static final ResourceLocation SHOW_OWNER = new ResourceLocation(SecurityCraft.MODID, "showowner");
	private static final ResourceLocation SHOW_MODULES = new ResourceLocation(SecurityCraft.MODID, "showmodules");
	private static final ResourceLocation SHOW_CUSTOM_NAME = new ResourceLocation(SecurityCraft.MODID, "showcustomname");
	private static final String LOCKED_TAG = "securitycraft_locked";

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(this, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(SHOW_OWNER, true);
		registration.addConfig(SHOW_MODULES, true);
		registration.addConfig(SHOW_CUSTOM_NAME, true);
		registration.registerBlockComponent(this, Block.class);
	}

	/** Server side: does this generic Key Panel lock cover the position being looked at? */
	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
		if (accessor.getLevel() instanceof ServerLevel level && ContainerLockData.get(level).isLocked(accessor.getPosition()))
			tag.putBoolean(LOCKED_TAG, true);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().getBoolean(LOCKED_TAG))
			tooltip.add(0, Utils.localize("waila.securitycraft:passcodeProtected").withStyle(ChatFormatting.GOLD));

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
	public ResourceLocation getUid() {
		return ID;
	}
}
