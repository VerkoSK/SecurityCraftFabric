package net.geforcemods.securitycraft.screen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.fabricmc.loader.api.FabricLoader;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.items.SCManualItem;
import net.geforcemods.securitycraft.misc.ManualPage;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.PageGroup;
import net.geforcemods.securitycraft.misc.SCManualPage;
import net.geforcemods.securitycraft.misc.StillValid;
import net.geforcemods.securitycraft.screen.components.HoverChecker;
import net.geforcemods.securitycraft.screen.components.IngredientDisplay;
import net.geforcemods.securitycraft.screen.components.TextHoverChecker;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * The SecurityCraft Manual's screen. 1:1 with upstream's screen of the same name, with the following Fabric/port
 * adjustments:
 * <ul>
 * <li>Forge's {@code ScrollPanel} does not exist here, so the patron list draws its own scrollable text list on a
 * plain {@link AbstractWidget} instead.
 * <li>{@code FMLEnvironment.production} is replaced with {@code !FabricLoader.getInstance().isDevelopmentEnvironment()}.
 * <li>The lens custom-color exclusion in the shaped-recipe search is dropped: this port's {@code SCContent.LENS} is a
 * plain {@link Item} with no dye-coloring feature yet (see the V0.5 lens-coloring backlog item), so there is nothing
 * to exclude.
 * <li>The view-activated and lockable feature icons are dropped: this port has no {@code IViewActivated} or
 * {@code ILockable} API, so those icons could never light up.
 * </ul>
 */
public class SCManualScreen extends Screen implements StillValid {
	private static final ResourceLocation PAGE = SCContent.id("textures/gui/info_book_texture.png");
	private static final ResourceLocation PAGE_WITH_SCROLL = SCContent.id("textures/gui/info_book_texture_special.png"); //for items without a recipe
	private static final ResourceLocation TITLE_PAGE = SCContent.id("textures/gui/info_book_title_page.png");
	private static final ResourceLocation ICONS = SCContent.id("textures/gui/info_book_icons.png");
	private static final int SUBPAGE_LENGTH = 1285;
	/** The Fabric port's own title page. It is the first page of the book; {@link #ORIGINAL_TITLE_PAGE} is the last. */
	private static final int PORT_TITLE_PAGE = -2;
	/** The original mod's title page, with its authors and patrons. It sits at the very back of the book. */
	private static final int ORIGINAL_TITLE_PAGE = -1;
	private static int lastPage = PORT_TITLE_PAGE;
	private final MutableComponent intro1 = Utils.localize("gui.securitycraft:scManual.intro.1").setStyle(Style.EMPTY.withUnderlined(true));
	private final Component ourPatrons = Utils.localize("gui.securitycraft:scManual.patreon.title");
	private final MutableComponent portTitle = Utils.localize("gui.securitycraft:scManual.port.title").setStyle(Style.EMPTY.withUnderlined(true));
	private final Component portedBy = Utils.localize("gui.securitycraft:scManual.portedBy");
	//scroll notches are fractional on trackpads and free-spinning wheels, so they are added up until they make a page
	private double scrolledSinceLastPage;
	private List<HoverChecker> hoverCheckers = new ArrayList<>();
	private int currentPage = lastPage;
	//1.21.2+ removed the empty Ingredient, so a null entry here means "no ingredient in this slot"
	private List<Ingredient> recipe;
	private IngredientDisplay[] displays = new IngredientDisplay[9];
	private int startX = -1;
	private List<FormattedText> subpages = new ArrayList<>();
	private List<FormattedCharSequence> author = new ArrayList<>();
	private int currentSubpage = 0;
	private List<FormattedCharSequence> intro2;
	private PatronList patronList;
	private Button patreonLinkButton;
	private Button nextSubpage;
	private Button previousSubpage;
	private boolean explosive, ownable, passcodeProtected, customizable, moduleInventory;
	private IngredientDisplay pageIcon;
	private Component pageTitle, designedBy;

	static {
		if (SCManualItem.PAGES.isEmpty())
			ManualPage.register();
	}

	public SCManualScreen() {
		super(Component.translatable(SCContent.SC_MANUAL.getDescriptionId()));
	}

	@Override
	public void init() {
		byte startY = 2;

		startX = (width - 256) / 2;
		patreonLinkButton = addRenderableWidget(new HyperlinkButton(startX + 225, 143, 16, 16, Component.empty(), b -> handleComponentClicked(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create("https://www.patreon.com/Geforce"))))));
		patronList = addRenderableWidget(new PatronList(112, 90, 90, startX + 130));
		patronList.fetchPatrons();
		previousSubpage = addRenderableWidget(new ChangePageButton(startX + 155, startY + 95, false, b -> previousSubpage()));
		nextSubpage = addRenderableWidget(new ChangePageButton(startX + 180, startY + 95, true, b -> nextSubpage()));
		addRenderableWidget(new ChangePageButton(startX + 22, startY + 188, false, b -> previousPage()));
		addRenderableWidget(new ChangePageButton(startX + 210, startY + 188, true, b -> nextPage()));

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				displays[(i * 3) + j] = addRenderableOnly(new IngredientDisplay((startX + 101) + (j * 19), 145 + (i * 19)));
			}
		}

		pageIcon = addRenderableOnly(new IngredientDisplay(startX + 19, 22));
		updateRecipeAndIcons();
		SCManualItem.PAGES.sort((page1, page2) -> {
			String key1 = page1.title().getString();
			String key2 = page2.title().getString();

			return key1.compareTo(key2);
		});
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, currentPage < 0 ? TITLE_PAGE : (recipe != null && !recipe.isEmpty() ? PAGE : PAGE_WITH_SCROLL), startX, 5, 0, 0, 256, 250, 256, 256);

		for (Renderable renderable : renderables) {
			renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
		}

		if (currentPage > -1) {
			String pageNumberText = (currentPage + 2) + "/" + (SCManualItem.PAGES.size() + 2); //+2 because neither title page is in the list

			if (subpages.size() > 1)
				guiGraphics.drawString(font, (currentSubpage + 1) + "/" + subpages.size(), startX + 205, 100, 0xFF8E8270, false);

			if (designedBy != null)
				guiGraphics.drawWordWrap(font, designedBy, startX + 18, 150, 75, 0xFF000000, false);

			guiGraphics.drawString(font, pageTitle, startX + 39, 27, 0xFF000000, false);
			guiGraphics.drawWordWrap(font, subpages.get(currentSubpage), startX + 18, 45, 225, 0xFF000000, false);
			guiGraphics.drawString(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);

			if (ownable)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 29, 118, 1, 1, 16, 16, 256, 256);

			if (passcodeProtected)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 55, 118, 18, 1, 17, 16, 256, 256);

			if (explosive)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 107, 117, 54, 1, 18, 18, 256, 256);

			if (customizable)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 136, 118, 88, 1, 16, 16, 256, 256);

			if (moduleInventory)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 163, 118, 105, 1, 16, 16, 256, 256);

			if (customizable || moduleInventory)
				guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, startX + 213, 118, 72, 1, 16, 16, 256, 256);

			for (int i = 0; i < hoverCheckers.size(); i++) {
				HoverChecker chc = hoverCheckers.get(i);

				if (chc != null && chc.checkHover(mouseX, mouseY)) {
					if (chc instanceof TextHoverChecker thc && thc.getName() != null) {
						guiGraphics.setComponentTooltipForNextFrame(font, thc.getLines(), mouseX, mouseY);
						break;
					}
					else if (i < displays.length && !displays[i].getCurrentStack().isEmpty()) {
						guiGraphics.setTooltipForNextFrame(font, displays[i].getCurrentStack(), mouseX, mouseY);
						break;
					}
				}
			}
		}
		else if (currentPage == PORT_TITLE_PAGE) {
			String pageNumberText = "1/" + (SCManualItem.PAGES.size() + 2); //+2 because neither title page is in the list

			guiGraphics.drawString(font, portTitle, width / 2 - font.width(portTitle) / 2, 22, 0xFF000000, false);
			guiGraphics.drawString(font, portedBy, width / 2 - font.width(portedBy) / 2, 150, 0xFF000000, false);
			guiGraphics.drawString(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);
		}
		else { //the original's own title page, at the back of the book
			String pageNumberText = (SCManualItem.PAGES.size() + 2) + "/" + (SCManualItem.PAGES.size() + 2);

			guiGraphics.drawString(font, intro1, width / 2 - font.width(intro1) / 2, 22, 0xFF000000, false);

			for (int i = 0; i < intro2.size(); i++) {
				FormattedCharSequence text = intro2.get(i);

				guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, 150 + 10 * i, 0xFF000000, false);
			}

			for (int i = 0; i < author.size(); i++) {
				FormattedCharSequence text = author.get(i);

				guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, 180 + 10 * i, 0xFF000000, false);
			}

			guiGraphics.drawString(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);
			guiGraphics.drawString(font, ourPatrons, width / 2 - font.width(ourPatrons) / 2 + 34, 40, 0xFF000000, false);
		}
	}

	@Override
	public void tick() {
		super.tick();
		pageIcon.tick();

		for (IngredientDisplay display : displays) {
			display.tick();
		}
	}

	@Override
	public void removed() {
		super.removed();
		lastPage = currentPage;
	}

	private void hideSubpageButtonsOnMainPage() {
		nextSubpage.visible = currentPage >= 0 && subpages.size() > 1;
		previousSubpage.visible = currentPage >= 0 && subpages.size() > 1;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (Screen.hasShiftDown()) {
			for (IngredientDisplay display : displays) {
				if (display != null)
					display.changeRenderingStack(-scrollY);
			}

			if (pageIcon != null)
				pageIcon.changeRenderingStack(-scrollY);

			return true;
		}

		if (currentPage == ORIGINAL_TITLE_PAGE && patronList != null && patronList.isMouseOver(mouseX, mouseY) && !patronList.patrons.isEmpty())
			return patronList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		if (Screen.hasControlDown() && subpages.size() > 1) {
			switch ((int) Math.signum(scrollY)) {
				case -1:
					nextSubpage();
					break;
				case 1:
					previousSubpage();
					break;
			}

			return true;
		}

		scrolledSinceLastPage += scrollY;

		while (scrolledSinceLastPage <= -1.0) {
			scrolledSinceLastPage++;
			nextPage();
		}

		while (scrolledSinceLastPage >= 1.0) {
			scrolledSinceLastPage--;
			previousPage();
		}

		//hide subpage buttons on main page
		hideSubpageButtonsOnMainPage();
		return true;
	}

	private void nextPage() {
		if (currentPage == PORT_TITLE_PAGE)
			currentPage = 0;
		else if (currentPage == ORIGINAL_TITLE_PAGE)
			currentPage = PORT_TITLE_PAGE;
		else if (++currentPage > SCManualItem.PAGES.size() - 1)
			currentPage = ORIGINAL_TITLE_PAGE;

		updateRecipeAndIcons();
		hideSubpageButtonsOnMainPage();
	}

	private void previousPage() {
		if (currentPage == PORT_TITLE_PAGE)
			currentPage = ORIGINAL_TITLE_PAGE;
		else if (currentPage == ORIGINAL_TITLE_PAGE)
			currentPage = SCManualItem.PAGES.size() - 1;
		else if (--currentPage < 0)
			currentPage = PORT_TITLE_PAGE;

		updateRecipeAndIcons();
		hideSubpageButtonsOnMainPage();
	}

	private void nextSubpage() {
		currentSubpage++;

		if (currentSubpage == subpages.size())
			currentSubpage = 0;
	}

	private void previousSubpage() {
		currentSubpage--;

		if (currentSubpage == -1)
			currentSubpage = subpages.size() - 1;
	}

	private void updateRecipeAndIcons() {
		currentSubpage = 0;
		hoverCheckers.clear();
		patreonLinkButton.visible = currentPage == ORIGINAL_TITLE_PAGE;

		if (currentPage < 0) {
			for (IngredientDisplay display : displays) {
				display.setIngredient(null);
			}

			pageIcon.setIngredient(null);
			recipe = null;
			nextSubpage.visible = false;
			previousSubpage.visible = false;

			if (I18n.exists("gui.securitycraft:scManual.author"))
				author = font.split(Utils.localize("gui.securitycraft:scManual.author"), 180);
			else
				author.clear();

			intro2 = font.split(Utils.localize("gui.securitycraft:scManual.intro.2"), 202);
			patronList.fetchPatrons();
			return;
		}

		SCManualPage page = SCManualItem.PAGES.get(currentPage);
		String designerName = page.designedBy();
		Item item = page.item();
		PageGroup pageGroup = page.group();

		if (designerName != null && !designerName.isEmpty())
			this.designedBy = Utils.localize("gui.securitycraft:scManual.designedBy", designerName);
		else
			this.designedBy = null;

		recipe = null;

		if (pageGroup == PageGroup.NONE) {
			Level level = Minecraft.getInstance().level;
			net.minecraft.util.context.ContextMap contextMap = net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level);

			outer:
			for (net.minecraft.client.gui.screens.recipebook.RecipeCollection collection : Minecraft.getInstance().player.getRecipeBook().getCollections()) {
				for (net.minecraft.world.item.crafting.display.RecipeDisplayEntry entry : collection.getRecipes()) {
					net.minecraft.world.item.crafting.display.RecipeDisplay display = entry.display();

					if (display.result().resolveForStacks(contextMap).stream().noneMatch(stack -> stack.is(item)))
						continue;

					if (display instanceof net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay shaped) {
						List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients = shaped.ingredients();
						List<Ingredient> recipeItems = new java.util.ArrayList<>(java.util.Collections.nCopies(9, (Ingredient) null));

						for (int i = 0; i < ingredients.size(); i++) {
							List<ItemStack> stacks = ingredients.get(i).resolveForStacks(contextMap);

							if (!stacks.isEmpty())
								recipeItems.set(getCraftMatrixPosition(i, shaped.width(), shaped.height()), Ingredient.of(stacks.stream().map(ItemStack::getItem)));
						}

						this.recipe = recipeItems;
						break outer;
					}
					else if (display instanceof net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay shapeless) {
						List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients = shapeless.ingredients();
						List<Ingredient> recipeItems = new java.util.ArrayList<>(java.util.Collections.nCopies(ingredients.size(), (Ingredient) null));

						for (int i = 0; i < ingredients.size(); i++) {
							List<ItemStack> stacks = ingredients.get(i).resolveForStacks(contextMap);

							if (!stacks.isEmpty())
								recipeItems.set(i, Ingredient.of(stacks.stream().map(ItemStack::getItem)));
						}

						this.recipe = recipeItems;
						break outer;
					}
				}
			}
		}
		else if (pageGroup.hasRecipeGrid()) {
			Level level = Minecraft.getInstance().level;
			net.minecraft.util.context.ContextMap contextMap = net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level);
			java.util.Map<Integer, ItemStack[]> recipeStacks = new java.util.HashMap<>();
			List<Item> pageItems = pageGroup.getItems() == null ? List.of() : pageGroup.getItems().items().map(net.minecraft.core.Holder::value).toList();
			int stacksLeft = pageItems.size();

			for (int i = 0; i < 9; i++) {
				recipeStacks.put(i, new ItemStack[pageItems.size()]);
			}

			outer:
			for (net.minecraft.client.gui.screens.recipebook.RecipeCollection collection : Minecraft.getInstance().player.getRecipeBook().getCollections()) {
				for (net.minecraft.world.item.crafting.display.RecipeDisplayEntry entry : collection.getRecipes()) {
					if (stacksLeft == 0)
						break outer;

					net.minecraft.world.item.crafting.display.RecipeDisplay display = entry.display();
					List<ItemStack> resultStacks = display.result().resolveForStacks(contextMap);
					ItemStack resultItem = resultStacks.isEmpty() ? ItemStack.EMPTY : resultStacks.get(0);

					if (resultItem.isEmpty() || !pageItems.contains(resultItem.getItem()))
						continue;

					int indexToAddAt = pageItems.indexOf(resultItem.getItem());

					if (display instanceof net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay shaped) {
						List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients = shaped.ingredients();

						for (int i = 0; i < ingredients.size(); i++) {
							List<ItemStack> items = ingredients.get(i).resolveForStacks(contextMap);

							if (items.isEmpty())
								continue;

							//first item needs to suffice since multiple recipes are being cycled through
							recipeStacks.get(getCraftMatrixPosition(i, shaped.width(), shaped.height()))[indexToAddAt] = items.get(0);
						}

						stacksLeft--;
					}
					else if (display instanceof net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay shapeless) {
						List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients = shapeless.ingredients();

						for (int i = 0; i < ingredients.size(); i++) {
							List<ItemStack> items = ingredients.get(i).resolveForStacks(contextMap);

							if (items.isEmpty())
								continue;

							recipeStacks.get(i)[indexToAddAt] = items.get(0);
						}

						stacksLeft--;
					}
				}
			}

			recipe = new java.util.ArrayList<>(java.util.Collections.nCopies(9, (Ingredient) null));
			recipeStacks.forEach((i, stackArray) -> recipe.set(i, ingredientOrNull(java.util.Arrays.stream(stackArray).filter(s -> s != null && !s.isEmpty()).map(ItemStack::getItem))));
		}

		if (page.hasRecipeDescription()) {
			String name = BuiltInRegistries.ITEM.getKey(page.item()).getPath();

			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.recipe." + name)));
		}
		else if (pageGroup == PageGroup.REINFORCED) {
			recipe = null;
			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.recipe.reinforced")));
		}
		else if (recipe != null) {
			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 3; column++) {
					hoverCheckers.add(new HoverChecker(144 + (row * 19), 144 + (row * 19) + 16, (startX + 101) + (column * 19), (startX + 101) + (column * 19) + 16));
				}
			}
		}
		else
			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.disabled")));

		pageTitle = page.title();

		if (pageGroup != PageGroup.NONE)
			pageIcon.setIngredient(pageGroup.getItems());
		else
			pageIcon.setIngredient(Ingredient.of(page.item()));

		resetBlockEntityInfo();

		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();

			explosive = block instanceof IExplosive;

			if (explosive)
				hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 107, (startX + 107) + 16, Utils.localize("gui.securitycraft:scManual.explosiveBlock")));
		}

		Object inWorldObject = page.getInWorldObject();

		if (inWorldObject != null) {
			ownable = inWorldObject instanceof IOwnable;
			passcodeProtected = inWorldObject instanceof PasscodeProtected;

			if (ownable)
				hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 29, (startX + 29) + 16, Utils.localize("gui.securitycraft:scManual.ownableBlock")));

			if (passcodeProtected)
				hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 55, (startX + 55) + 16, Utils.localize("gui.securitycraft:scManual.passcodeProtectedBlock")));

			if (inWorldObject instanceof ICustomizable customizableObj) {
				Option<?>[] options = customizableObj.customOptions();

				if (options.length > 0) {
					List<Component> display = new ArrayList<>();

					customizable = true;
					display.add(Utils.localize("gui.securitycraft:scManual.options"));
					display.add(Component.literal("---"));

					for (Option<?> option : options) {
						display.add(Component.translatable("gui.securitycraft:scManual.option_text", Component.translatable(option.getDescriptionKey(Utils.getLanguageKeyDenotation(customizableObj))), option.getDefaultInfo()));
						display.add(Component.empty());
					}

					display.remove(display.size() - 1);
					hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 136, (startX + 136) + 16, display));
				}
			}

			if (inWorldObject instanceof IModuleInventory moduleInv && moduleInv.acceptedModules() != null && moduleInv.acceptedModules().length > 0) {
				List<Component> display = new ArrayList<>();

				moduleInventory = true;
				display.add(Utils.localize("gui.securitycraft:scManual.modules"));
				display.add(Component.literal("---"));

				for (ModuleType module : moduleInv.acceptedModules()) {
					display.add(Component.literal("- ").append(Utils.localize(moduleInv.getModuleDescriptionId(Utils.getLanguageKeyDenotation(moduleInv), module))));
					display.add(Component.empty());
				}

				display.remove(display.size() - 1);
				hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 163, (startX + 163) + 16, display));
			}

			if (customizable || moduleInventory)
				hoverCheckers.add(new TextHoverChecker(118, 118 + 16, startX + 213, (startX + 213) + 16, Utils.localize("gui.securitycraft:scManual.customizableBlock")));
		}

		if (recipe != null && !recipe.isEmpty()) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int index = (i * 3) + j;

					if (index >= recipe.size())
						displays[index].setIngredient(null);
					else
						displays[index].setIngredient(recipe.get(index));
				}
			}
		}
		else {
			for (IngredientDisplay display : displays) {
				display.setIngredient(null);
			}
		}

		//set up subpages
		subpages = font.getSplitter().splitLines(page.helpInfo(), SUBPAGE_LENGTH, Style.EMPTY);
		nextSubpage.visible = currentPage >= 0 && subpages.size() > 1;
		previousSubpage.visible = currentPage >= 0 && subpages.size() > 1;
	}

	private void resetBlockEntityInfo() {
		explosive = false;
		ownable = false;
		passcodeProtected = false;
		customizable = false;
		moduleInventory = false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (patronList != null)
			patronList.mouseClicked(mouseX, mouseY, button);

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (patronList != null)
			patronList.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(mouseX, mouseY, button);
	}

	/**
	 * Fabric stand-in for upstream's {@code ScrollPanel}-based patron list: Forge's {@code ScrollPanel} has no Fabric
	 * equivalent, so this draws its own scissored, scrollable text list on top of a plain {@link AbstractWidget}.
	 */
	class PatronList extends AbstractWidget {
		private final String patronListLink = FabricLoader.getInstance().isDevelopmentEnvironment() ? "https://gist.githubusercontent.com/bl4ckscor3/3196e6740774e386871a74a9606eaa61/raw" : "https://gist.githubusercontent.com/bl4ckscor3/bdda6596012b1206816db034350b5717/raw";
		private static final int ROW_HEIGHT = 12;
		private final ExecutorService executor = Executors.newSingleThreadExecutor();
		private Future<List<String>> patronRequestFuture;
		private List<String> patrons = new ArrayList<>();
		private boolean patronsAvailable = false;
		private boolean error = false;
		private boolean patronsRequested;
		private final List<FormattedCharSequence> fetchErrorLines;
		private final List<FormattedCharSequence> noPatronsLines;
		private final Component loadingText = Utils.localize("gui.securitycraft:scManual.patreon.loading");
		private double scrollDistance;

		public PatronList(int width, int height, int top, int left) {
			super(left, top, width, height, Component.empty());
			fetchErrorLines = font.split(Utils.localize("gui.securitycraft:scManual.patreon.error"), width);
			noPatronsLines = font.split(Component.translatable("advancements.empty"), width - 10);
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			if (currentPage != -1)
				return;

			if (patronsAvailable) {
				if (patrons.isEmpty()) {
					for (int i = 0; i < noPatronsLines.size(); i++) {
						FormattedCharSequence line = noPatronsLines.get(i);

						guiGraphics.drawString(font, line, getX() + width / 2 - font.width(line) / 2, getY() + 30 + i * 10, 0xFF333333, false);
					}

					return;
				}

				int maxScroll = Math.max(0, patrons.size() * ROW_HEIGHT - height);

				scrollDistance = Mth.clamp(scrollDistance, 0, maxScroll);
				guiGraphics.enableScissor(getX(), getY(), getX() + width, getY() + height);

				for (int i = 0; i < patrons.size(); i++) {
					String patron = patrons.get(i);
					int rowTop = getY() + i * ROW_HEIGHT - (int) scrollDistance;

					if (rowTop + ROW_HEIGHT < getY() || rowTop > getY() + height)
						continue;

					guiGraphics.drawString(font, patron, getX() + 2, rowTop + 2, 0xFF000000, false);

					if (mouseX >= getX() && mouseX < getX() + width - 6 && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT && font.width(patron) >= width - 6)
						guiGraphics.setTooltipForNextFrame(font, List.<Component>of(Component.literal(patron)), Optional.empty(), mouseX, rowTop);
				}

				guiGraphics.disableScissor();
			}
			else if (error) {
				for (int i = 0; i < fetchErrorLines.size(); i++) {
					FormattedCharSequence line = fetchErrorLines.get(i);

					guiGraphics.drawString(font, line, getX() + width / 2 - font.width(line) / 2, getY() + 30 + i * 10, 0xFFB00101, false);
				}
			}
			else if (patronRequestFuture != null && patronRequestFuture.isDone()) {
				try {
					patrons = patronRequestFuture.get();
					executor.shutdown();
					patronsAvailable = true;
				}
				catch (InterruptedException | ExecutionException e) {
					error = true;
				}
			}
			else
				guiGraphics.drawString(font, loadingText, getX() + width / 2 - font.width(loadingText) / 2, getY() + 30, 0xFF000000, false);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
			scrollDistance -= scrollY * ROW_HEIGHT;
			return true;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		public void fetchPatrons() {
			if (!patronsRequested) {
				//create thread to fetch patrons. without this, and for example if the player has no internet connection, the game will hang
				patronRequestFuture = executor.submit(() -> {
					try {
						//a plain URL#openStream sends no user agent and waits forever on a stalled connection, which is
						//what left the list stuck on "loading" instead of ever showing the patrons or the error
						HttpURLConnection connection = (HttpURLConnection) URI.create(patronListLink).toURL().openConnection();

						connection.setRequestProperty("User-Agent", "SecurityCraft-Fabric");
						connection.setConnectTimeout(5000);
						connection.setReadTimeout(5000);
						connection.setInstanceFollowRedirects(true);

						try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
							return reader.lines().filter(line -> !line.isBlank()).toList();
						}
					}
					catch (IOException e) {
						SecurityCraft.LOGGER.warn("Could not fetch the patron list", e);
						error = true;
						return new ArrayList<>();
					}
				});
				patronsRequested = true;
			}
		}
	}

	static class ChangePageButton extends Button {
		//1.21.x moved the book's page-turn arrows out of book.png and into these widget sprites
		private static final ResourceLocation FORWARD = ResourceLocation.withDefaultNamespace("widget/page_forward");
		private static final ResourceLocation FORWARD_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted");
		private static final ResourceLocation BACKWARD = ResourceLocation.withDefaultNamespace("widget/page_backward");
		private static final ResourceLocation BACKWARD_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted");
		private final boolean forward;

		public ChangePageButton(int xPos, int yPos, boolean forward, OnPress onPress) {
			super(xPos, yPos, 23, 13, Component.empty(), onPress, DEFAULT_NARRATION);
			this.forward = forward;
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			if (visible) {
				isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

				ResourceLocation sprite = isHoveredOrFocused() ? (forward ? FORWARD_HIGHLIGHTED : BACKWARD_HIGHLIGHTED) : (forward ? FORWARD : BACKWARD);

				guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), 23, 13);
			}
		}
	}

	static class HyperlinkButton extends Button {
		public HyperlinkButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
			super(xPos, yPos, width, height, displayString, handler, s -> Component.empty());
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
			guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS, getX(), getY(), isHoveredOrFocused() ? 138 : 122, 1, 16, 16, 256, 256);
		}
	}

	//from JEI
	/** 1.21.2+ forbids an empty Ingredient, so an empty item stream becomes null ("no ingredient") instead. */
	private static Ingredient ingredientOrNull(java.util.stream.Stream<Item> items) {
		List<Item> list = items.toList();

		return list.isEmpty() ? null : Ingredient.of(list.stream());
	}

	private int getCraftMatrixPosition(int i, int width, int height) {
		int index;

		if (width == 1) {
			if (height == 3)
				index = (i * 3) + 1;
			else if (height == 2)
				index = (i * 3) + 1;
			else
				index = 4;
		}
		else if (height == 1)
			index = i + 3;
		else if (width == 2) {
			index = i;

			if (i > 1) {
				index++;

				if (i > 3)
					index++;
			}
		}
		else if (height == 2)
			index = i + 3;
		else
			index = i;

		return index;
	}

	@Override
	public boolean stillValid(Player player) {
		return !PlayerUtils.getItemStackFromAnyHand(player, SCContent.SC_MANUAL).isEmpty();
	}
}
