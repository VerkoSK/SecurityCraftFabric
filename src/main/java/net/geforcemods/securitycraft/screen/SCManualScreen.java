package net.geforcemods.securitycraft.screen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * The SecurityCraft Manual's screen. Ported from upstream's screen of the same name, with the following Fabric/port
 * adjustments:
 * <ul>
 * <li>Forge's {@code ScrollPanel} does not exist here, so the patron list draws its own scrollable text list on a
 * plain {@link AbstractWidget} instead.
 * <li>{@code FMLEnvironment.production} is replaced with {@code !FabricLoader.getInstance().isDevelopmentEnvironment()}.
 * <li>The in-book crafting-recipe grid is dropped: MC 1.21.6+ replaced the flat {@code Recipe}/{@code Ingredient}
 * lookup this used with the {@code RecipeDisplay}/{@code SlotDisplay} system, which has no cheap client-side
 * equivalent. The manual still shows every page, its text, icons and the patron list. Recorded in {@code PORT_GAP.md}.
 * <li>The view-activated and lockable feature icons are dropped: this port has no {@code IViewActivated} or
 * {@code ILockable} API, so those icons could never light up.
 * </ul>
 */
public class SCManualScreen extends Screen implements StillValid {
	private static final Identifier PAGE_WITH_SCROLL = SCContent.id("textures/gui/info_book_texture_special.png");
	private static final Identifier TITLE_PAGE = SCContent.id("textures/gui/info_book_title_page.png");
	private static final Identifier ICONS = SCContent.id("textures/gui/info_book_icons.png");
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

	private static boolean shiftDown() {
		com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();

		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	private static boolean controlDown() {
		com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();

		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
	}

	@Override
	public void init() {
		byte startY = 2;

		startX = (width - 256) / 2;
		patreonLinkButton = addRenderableWidget(new HyperlinkButton(startX + 225, 143, 16, 16, b -> net.minecraft.util.Util.getPlatform().openUri(URI.create("https://www.patreon.com/Geforce"))));
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
		SCManualItem.PAGES.sort((page1, page2) -> page1.title().getString().compareTo(page2.title().getString()));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(extractor, mouseX, mouseY, partialTick);
		extractor.blit(RenderPipelines.GUI_TEXTURED, currentPage < 0 ? TITLE_PAGE : PAGE_WITH_SCROLL, startX, 5, 0.0F, 0.0F, 256, 250, 256, 256);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(extractor, mouseX, mouseY, partialTick);

		if (currentPage > -1) {
			String pageNumberText = (currentPage + 2) + "/" + (SCManualItem.PAGES.size() + 2); //+2 because neither title page is in the list

			if (subpages.size() > 1)
				extractor.text(font, (currentSubpage + 1) + "/" + subpages.size(), startX + 205, 100, 0xFF8E8270, false);

			if (designedBy != null)
				extractor.textWithWordWrap(font, designedBy, startX + 18, 150, 75, 0xFF000000);

			extractor.text(font, pageTitle, startX + 39, 27, 0xFF000000, false);
			extractor.textWithWordWrap(font, subpages.get(currentSubpage), startX + 18, 45, 225, 0xFF000000);
			extractor.text(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);

			if (ownable)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 29, 118, 1.0F, 1.0F, 16, 16, 256, 256);

			if (passcodeProtected)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 55, 118, 18.0F, 1.0F, 17, 16, 256, 256);

			if (explosive)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 107, 117, 54.0F, 1.0F, 18, 18, 256, 256);

			if (customizable)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 136, 118, 88.0F, 1.0F, 16, 16, 256, 256);

			if (moduleInventory)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 163, 118, 105.0F, 1.0F, 16, 16, 256, 256);

			if (customizable || moduleInventory)
				extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, startX + 213, 118, 72.0F, 1.0F, 16, 16, 256, 256);

			for (int i = 0; i < hoverCheckers.size(); i++) {
				HoverChecker chc = hoverCheckers.get(i);

				if (chc != null && chc.checkHover(mouseX, mouseY)) {
					if (chc instanceof TextHoverChecker thc && thc.getName() != null) {
						extractor.setTooltipForNextFrame(font, thc.getLines(), Optional.empty(), mouseX, mouseY);
						break;
					}
					else if (i < displays.length && !displays[i].getCurrentStack().isEmpty()) {
						extractor.setTooltipForNextFrame(font, displays[i].getCurrentStack(), mouseX, mouseY);
						break;
					}
				}
			}
		}
		else if (currentPage == PORT_TITLE_PAGE) {
			String pageNumberText = "1/" + (SCManualItem.PAGES.size() + 2);

			extractor.text(font, portTitle, width / 2 - font.width(portTitle) / 2, 22, 0xFF000000, false);
			extractor.text(font, portedBy, width / 2 - font.width(portedBy) / 2, 150, 0xFF000000, false);
			extractor.text(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);
		}
		else { //the original's own title page, at the back of the book
			String pageNumberText = (SCManualItem.PAGES.size() + 2) + "/" + (SCManualItem.PAGES.size() + 2);

			extractor.text(font, intro1, width / 2 - font.width(intro1) / 2, 22, 0xFF000000, false);

			for (int i = 0; i < intro2.size(); i++) {
				FormattedCharSequence text = intro2.get(i);

				extractor.text(font, text, width / 2 - font.width(text) / 2, 150 + 10 * i, 0xFF000000, false);
			}

			for (int i = 0; i < author.size(); i++) {
				FormattedCharSequence text = author.get(i);

				extractor.text(font, text, width / 2 - font.width(text) / 2, 180 + 10 * i, 0xFF000000, false);
			}

			extractor.text(font, pageNumberText, startX + 240 - font.width(pageNumberText), 182, 0xFF8E8270, false);
			extractor.text(font, ourPatrons, width / 2 - font.width(ourPatrons) / 2 + 34, 40, 0xFF000000, false);
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
		double scroll = scrollY;

		if (shiftDown()) {
			for (IngredientDisplay display : displays) {
				if (display != null)
					display.changeRenderingStack(-scroll);
			}

			if (pageIcon != null)
				pageIcon.changeRenderingStack(-scroll);

			return true;
		}

		if (currentPage == ORIGINAL_TITLE_PAGE && patronList != null && patronList.isMouseOver(mouseX, mouseY) && !patronList.patrons.isEmpty())
			return patronList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		if (controlDown() && subpages.size() > 1) {
			switch ((int) Math.signum(scroll)) {
				case -1:
					nextSubpage();
					break;
				case 1:
					previousSubpage();
					break;
			}

			return true;
		}

		scrolledSinceLastPage += scroll;

		while (scrolledSinceLastPage <= -1.0) {
			scrolledSinceLastPage++;
			nextPage();
		}

		while (scrolledSinceLastPage >= 1.0) {
			scrolledSinceLastPage--;
			previousPage();
		}

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
				display.setStacks(List.of());
			}

			pageIcon.setStacks(List.of());
			nextSubpage.visible = false;
			previousSubpage.visible = false;

			if (net.minecraft.locale.Language.getInstance().has("gui.securitycraft:scManual.author"))
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

		if (page.hasRecipeDescription()) {
			String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(page.item()).getPath();

			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.recipe." + name)));
		}
		else if (pageGroup == PageGroup.REINFORCED)
			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.recipe.reinforced")));
		else
			hoverCheckers.add(new TextHoverChecker(144, 144 + (2 * 20) + 16, startX + 100, (startX + 100) + (2 * 20) + 16, Utils.localize("gui.securitycraft:scManual.disabled")));

		pageTitle = page.title();

		if (pageGroup != PageGroup.NONE)
			pageIcon.setStacks(pageGroup.getItems());
		else
			pageIcon.setStacks(List.of(new ItemStack(page.item())));

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

		for (IngredientDisplay display : displays) {
			display.setStacks(List.of());
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (patronList != null)
			patronList.mouseClicked(event, doubleClick);

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (patronList != null)
			patronList.mouseReleased(event);

		return super.mouseReleased(event);
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
		protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
			if (currentPage != -1)
				return;

			if (patronsAvailable) {
				if (patrons.isEmpty()) {
					for (int i = 0; i < noPatronsLines.size(); i++) {
						FormattedCharSequence line = noPatronsLines.get(i);

						extractor.text(font, line, getX() + width / 2 - font.width(line) / 2, getY() + 30 + i * 10, 0xFF333333, false);
					}

					return;
				}

				int maxScroll = Math.max(0, patrons.size() * ROW_HEIGHT - height);

				scrollDistance = Mth.clamp(scrollDistance, 0, maxScroll);
				extractor.enableScissor(getX(), getY(), getX() + width, getY() + height);

				for (int i = 0; i < patrons.size(); i++) {
					String patron = patrons.get(i);
					int rowTop = getY() + i * ROW_HEIGHT - (int) scrollDistance;

					if (rowTop + ROW_HEIGHT < getY() || rowTop > getY() + height)
						continue;

					extractor.text(font, patron, getX() + 2, rowTop + 2, 0xFF000000, false);

					if (mouseX >= getX() && mouseX < getX() + width - 6 && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT && font.width(patron) >= width - 6)
						extractor.setTooltipForNextFrame(font, List.of(Component.literal(patron)), Optional.empty(), mouseX, rowTop);
				}

				extractor.disableScissor();
			}
			else if (error) {
				for (int i = 0; i < fetchErrorLines.size(); i++) {
					FormattedCharSequence line = fetchErrorLines.get(i);

					extractor.text(font, line, getX() + width / 2 - font.width(line) / 2, getY() + 30 + i * 10, 0xFFB00101, false);
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
				extractor.text(font, loadingText, getX() + width / 2 - font.width(loadingText) / 2, getY() + 30, 0xFF000000, false);
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
				patronRequestFuture = executor.submit(() -> {
					try {
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
						return new ArrayList<String>();
					}
				});
				patronsRequested = true;
			}
		}
	}

	static class ChangePageButton extends Button.Plain {
		//1.21.x moved the book's page-turn arrows out of book.png and into these widget sprites
		private static final Identifier FORWARD = Identifier.withDefaultNamespace("widget/page_forward");
		private static final Identifier FORWARD_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/page_forward_highlighted");
		private static final Identifier BACKWARD = Identifier.withDefaultNamespace("widget/page_backward");
		private static final Identifier BACKWARD_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/page_backward_highlighted");
		private final boolean forward;

		public ChangePageButton(int xPos, int yPos, boolean forward, OnPress onPress) {
			super(xPos, yPos, 23, 13, Component.empty(), onPress, DEFAULT_NARRATION);
			this.forward = forward;
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
			if (visible) {
				Identifier sprite = isHoveredOrFocused() ? (forward ? FORWARD_HIGHLIGHTED : BACKWARD_HIGHLIGHTED) : (forward ? FORWARD : BACKWARD);

				extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), 23, 13);
			}
		}
	}

	static class HyperlinkButton extends Button.Plain {
		public HyperlinkButton(int xPos, int yPos, int width, int height, OnPress handler) {
			super(xPos, yPos, width, height, Component.empty(), handler, DEFAULT_NARRATION);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
			extractor.blit(RenderPipelines.GUI_TEXTURED, ICONS, getX(), getY(), isHoveredOrFocused() ? 138.0F : 122.0F, 1.0F, 16, 16, 256, 256);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return !PlayerUtils.getItemStackFromAnyHand(player, SCContent.SC_MANUAL).isEmpty();
	}
}
