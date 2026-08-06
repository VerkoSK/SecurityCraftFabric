package net.geforcemods.securitycraft.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.screen.BlockReinforcerScreen;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI integration. The keypad has no crafting recipe (it is made by converting a Frame with a Key Panel),
 * so looking it up in JEI shows an information page explaining that, exactly like the original mod.
 *
 * <p>On Fabric this class must be registered via the {@code jei_mod_plugin} entrypoint in fabric.mod.json —
 * the {@code @JeiPlugin} annotation alone is only scanned on (Neo)Forge.
 */
@JeiPlugin
public class SCJEIPlugin implements IModPlugin {
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addIngredientInfo(new ItemStack(SCContent.KEYPAD), VanillaTypes.ITEM_STACK, Utils.localize("gui.securitycraft:scManual.recipe.keypad"));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(BlockReinforcerScreen.class, new SlotMover<>());
	}

	@Override
	public ResourceLocation getPluginUid() {
		return SCContent.id("jei_plugin");
	}
}
