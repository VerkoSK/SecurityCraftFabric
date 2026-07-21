package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.geforcemods.securitycraft.network.SetListModuleDataPayload;
import net.geforcemods.securitycraft.screen.components.CallbackCheckbox;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Editor for the allow/deny-list module: add/remove player names and toggle "affect everyone". A functional
 * Fabric reimplementation of the upstream screen (which uses NeoForge's {@code ScrollPanel}; team editing is
 * left out as team ownership isn't ported).
 */
public class EditModuleScreen extends Screen {
	private static final int X_SIZE = 200;
	private static final int Y_SIZE = 184;
	private static final int LIST_X = 10;
	private static final int LIST_Y = 34;
	private static final int LIST_W = 108;
	private static final int ROW_H = 12;
	private static final int VISIBLE = 11;
	private final ItemStack module;
	private final List<String> players = new ArrayList<>();
	private final List<String> teams;
	private boolean affectEveryone;
	private EditBox inputField;
	private int selectedIndex = -1;
	private int scrollOffset = 0;
	private int leftPos;
	private int topPos;

	public EditModuleScreen(ItemStack module) {
		super(Utils.localize("gui.securitycraft:editModule"));
		this.module = module;

		ListModuleData data = module.getOrDefault(SCContent.LIST_MODULE_DATA, ListModuleData.EMPTY);

		players.addAll(data.players());
		teams = new ArrayList<>(data.teams());
		affectEveryone = data.affectEveryone();
	}

	@Override
	protected void init() {
		leftPos = (width - X_SIZE) / 2;
		topPos = (height - Y_SIZE) / 2;

		int cx = leftPos + 128;

		inputField = addRenderableWidget(new EditBox(font, cx, topPos + 20, 62, 16, Component.empty()) {
			@Override
			public boolean keyPressed(KeyEvent event) {
				if (isFocused() && (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER)) {
					addPlayer();
					return true;
				}

				return super.keyPressed(event);
			}
		});
		inputField.setMaxLength(16);
		setInitialFocus(inputField);
		addRenderableWidget(Button.builder(local("add_player"), b -> addPlayer()).bounds(cx, topPos + 40, 62, 18).build());
		addRenderableWidget(Button.builder(local("remove_player"), b -> removePlayer()).bounds(cx, topPos + 62, 62, 18).build());
		addRenderableWidget(Button.builder(local("clear"), b -> clear()).bounds(cx, topPos + 84, 62, 18).build());
		addRenderableWidget(new CallbackCheckbox(cx, topPos + 112, 20, 20, local("affect_everyone"), affectEveryone, newValue -> {
			affectEveryone = newValue;
			save();
		}, 0xFFFFFF));
	}

	private Component local(String key) {
		return Utils.localize("gui.securitycraft:editModule." + key);
	}

	private void addPlayer() {
		String name = inputField.getValue().trim();

		if (!name.isEmpty() && players.size() < ListModuleData.MAX_PLAYERS && players.stream().noneMatch(name::equalsIgnoreCase)) {
			players.add(name);
			inputField.setValue("");
			save();
		}
	}

	private void removePlayer() {
		if (selectedIndex >= 0 && selectedIndex < players.size()) {
			players.remove(selectedIndex);
			selectedIndex = -1;
			save();
		}
	}

	private void clear() {
		players.clear();
		selectedIndex = -1;
		save();
	}

	private void save() {
		ListModuleData data = new ListModuleData(new ArrayList<>(players), teams, affectEveryone);

		module.set(SCContent.LIST_MODULE_DATA, data);
		ClientPlayNetworking.send(new SetListModuleDataPayload(data));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(extractor, mouseX, mouseY, partialTick);
		extractor.fill(leftPos, topPos, leftPos + X_SIZE, topPos + Y_SIZE, 0xF01A1A1A);
		extractor.centeredText(font, title, leftPos + X_SIZE / 2, topPos + 6, 0xFFFFFF);

		int lx = leftPos + LIST_X;
		int ly = topPos + LIST_Y;

		extractor.fill(lx - 1, ly - 1, lx + LIST_W + 1, ly + VISIBLE * ROW_H + 1, 0xFF000000);

		for (int i = 0; i < VISIBLE && scrollOffset + i < players.size(); i++) {
			int idx = scrollOffset + i;
			int ry = ly + i * ROW_H;

			if (idx == selectedIndex)
				extractor.fill(lx, ry, lx + LIST_W, ry + ROW_H, 0xFF335588);

			extractor.text(font, players.get(idx), lx + 2, ry + 2, 0xFFFFFF, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int lx = leftPos + LIST_X;
		int ly = topPos + LIST_Y;
		double mouseX = event.x();
		double mouseY = event.y();

		if (mouseX >= lx && mouseX <= lx + LIST_W && mouseY >= ly && mouseY < ly + VISIBLE * ROW_H) {
			int row = (int) ((mouseY - ly) / ROW_H) + scrollOffset;

			selectedIndex = row < players.size() ? row : -1;
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int max = Math.max(0, players.size() - VISIBLE);

		scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(scrollY)));
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!inputField.isFocused() && minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void tick() {
		// Auto-close if the module item is no longer held (upstream StillValid behaviour).
		if (minecraft.player == null || net.geforcemods.securitycraft.util.PlayerUtils.getItemStackFromAnyHand(minecraft.player, module.getItem()).isEmpty())
			onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
