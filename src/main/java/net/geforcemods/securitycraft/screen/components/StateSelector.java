package net.geforcemods.securitycraft.screen.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.joml.Quaternionf;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.inventory.StateSelectorAccessMenu;
import net.geforcemods.securitycraft.util.BlockEntityRenderDelegate;
import net.geforcemods.securitycraft.util.FullbrightBlockAndTintGetter;
import net.geforcemods.securitycraft.util.StandingOrWallType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class StateSelector extends AbstractContainerEventHandler implements Renderable, NarratableEntry, ContainerListener {
	private static final ResourceLocation TEXTURE = SCContent.id("textures/gui/container/state_selector.png");
	private static final int PAGE_LENGTH = 5;
	private static final Quaternionf BLOCK_ROTATION = Axis.XP.rotationDegrees(30).mul(Axis.YP.rotationDegrees(225));
	private final StateSelectorAccessMenu menu;
	private final int xStart, yStart, slotToCheck, previewXTranslation, previewYTranslation;
	private final List<Rect2i> extraAreas = new ArrayList<>();
	private final BlockEntityRenderDelegate renderDelegate = new BlockEntityRenderDelegate();
	private Block block = Blocks.AIR;
	private BlockState state = Blocks.AIR.defaultBlockState();
	private List<Property<?>> properties = List.of();
	private BlockEntity be = null;
	private FullbrightBlockAndTintGetter fullbrightLevel;
	private BlockStatePropertyButton<?>[] buttons = new BlockStatePropertyButton[PAGE_LENGTH];
	private int page, amountOfPages;
	private StandingOrWallType standingOrWallType = StandingOrWallType.NONE;
	private Button previousPageButton, nextPageButton;
	private HoverChecker standingOrWallTooltip;
	private ToggleComponentButton standingOrWallButton = null;
	private List<AbstractWidget> widgets = new ArrayList<>();
	private float dragX = -150.0F;
	private float dragY = -180.0F;

	public StateSelector(StateSelectorAccessMenu menu, int titleX, int xStart, int yStart, int slotToCheck, int dragX, int dragY) {
		this(menu, null, xStart, yStart, slotToCheck, dragX, dragY, 38, 63);
	}

	public StateSelector(StateSelectorAccessMenu menu, Component title, int xStart, int yStart, int slotToCheck, int dragStartX, int dragStartY, int previewXTranslation, int previewYTranslation) {
		this.menu = menu;
		this.xStart = xStart;
		this.yStart = yStart;
		this.slotToCheck = slotToCheck;
		this.dragX = dragStartX;
		this.dragY = dragStartY;
		this.previewXTranslation = previewXTranslation;
		this.previewYTranslation = previewYTranslation;
		menu.addSlotListener(this);
	}

	public StateSelector(StateSelectorAccessMenu menu, int xStart, int yStart, int slotToCheck, int dragX, int dragY) {
		this(menu, null, xStart, yStart, slotToCheck, dragX, dragY, 38, 63);
	}

	public void init(Minecraft mc, int width, int height) {
		init();
	}

	public void init() {
		if (menu.getSavedState() != null) {
			state = menu.getSavedState();
			block = state.getBlock();

			if (menu.getStandingOrWallType() != StandingOrWallType.NONE)
				standingOrWallType = menu.getStandingOrWallType();

			updateBlockEntityInfo(true, false);
		}
		else
			updateFromStack(menu.getStateStack());

		previousPageButton = new Button(xStart + 69, yStart + 125, 20, 20, Component.literal("<"), button -> changePage(-1), Button.DEFAULT_NARRATION);
		nextPageButton = new Button(xStart + 126, yStart + 125, 20, 20, Component.literal(">"), button -> changePage(1), Button.DEFAULT_NARRATION);
		initButtons(true, false);
		widgets.add(previousPageButton);
		widgets.add(nextPageButton);
		extraAreas.add(new Rect2i(xStart, yStart, 193, 152));
		fullbrightLevel = new FullbrightBlockAndTintGetter(Minecraft.getInstance().level);
	}

	public void initButtons(boolean updateStandingOrWallButton, boolean slotChanged) {
		int xPos = xStart + 87;
		int yPos = yStart + 8;

		properties = new ArrayList<>(state.getProperties());
		widgets.removeIf(BlockStatePropertyButton.class::isInstance);

		for (int i = 0; i < PAGE_LENGTH; i++) {
			int listIndex = (page - 1) * PAGE_LENGTH + i;

			if (listIndex >= properties.size())
				buttons[i] = null;
			else {
				Property<?> property = properties.get(listIndex);
				List<?> values = List.copyOf(property.getPossibleValues());
				int defaultValueIndex = values.indexOf(state.getValue(property));

				buttons[i] = new BlockStatePropertyButton<>(xPos, yPos, 100, 20, defaultValueIndex, property);
				widgets.add(buttons[i]);
			}

			yPos += 23;
		}

		if (updateStandingOrWallButton) {
			if (standingOrWallButton != null)
				widgets.remove(standingOrWallButton);

			if (block.asItem() instanceof StandingAndWallBlockItem) {
				standingOrWallType = slotChanged || standingOrWallType == StandingOrWallType.NONE ? StandingOrWallType.STANDING : standingOrWallType;
				standingOrWallButton = new ToggleComponentButton(xStart + 29, yStart + 109, 20, 20, i -> Component.empty(), standingOrWallType.ordinal() - 1, 2, b -> {
					standingOrWallType = standingOrWallType == StandingOrWallType.STANDING ? StandingOrWallType.WALL : StandingOrWallType.STANDING;
					state = standingOrWallType.getState(block);
					updateBlockEntityInfo(false, true);
					initButtons(false, false);
					menu.onStateChange(state);
				});
				standingOrWallTooltip = new HoverChecker(standingOrWallButton);
				widgets.add(standingOrWallButton);
			}
			else {
				standingOrWallType = StandingOrWallType.NONE;
				standingOrWallButton = null;
				standingOrWallTooltip = null;
			}
		}

		previousPageButton.active = page > 1;
		nextPageButton.active = page != amountOfPages;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		PoseStack pose = guiGraphics.pose();
		Font font = Minecraft.getInstance().font;

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		guiGraphics.blit(TEXTURE, xStart, yStart, 0, 0, 193, 152);

		for (AbstractWidget widget : widgets) {
			widget.render(guiGraphics, mouseX, mouseY, partialTick);
		}

		if (standingOrWallButton != null)
			guiGraphics.blit(TEXTURE, standingOrWallButton.getX() + 2, standingOrWallButton.getY() + 2, standingOrWallType == StandingOrWallType.STANDING ? 24 : 8, 152, 16, 16);

		for (int i = 0; i < buttons.length; i++) {
			BlockStatePropertyButton<?> button = buttons[i];

			if (button != null) {
				String propertyName = button.getProperty().getName();
				int yOffset = i * 23 + 14;

				guiGraphics.drawString(font, propertyName, xStart + 87 / 2 - font.width(propertyName) / 2 + 41, yStart + yOffset, 0x404040, false);
			}
		}

		guiGraphics.drawString(font, page + "/" + amountOfPages, xStart + 102, yStart + 130, 0x404040, false);
		pose.pushPose();
		pose.translate(xStart + previewXTranslation, yStart + previewYTranslation, 100);
		pose.scale(30.0F, -30.0F, 30.0F);
		pose.mulPose(Axis.XP.rotationDegrees(dragY));
		pose.mulPose(Axis.YP.rotationDegrees(dragX));
		pose.translate(-0.5F, -0.5F, -0.5F);
		Lighting.setupForEntityInInventory();
		renderBlockModel(state, pose, guiGraphics.bufferSource());

		if (be != null)
			renderDelegate.tryRenderDelegate(be, 0.0F, pose, guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

		guiGraphics.flush();
		Lighting.setupFor3DItems();
		pose.popPose();
	}

	public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (standingOrWallTooltip != null && standingOrWallTooltip.checkHover(mouseX, mouseY))
			guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(standingOrWallButton.getCurrentIndex() == 0 ? "gui.securitycraft:stateSelector.standing" : "gui.securitycraft:stateSelector.wall"), mouseX, mouseY);
	}

	private void renderBlockModel(BlockState state, PoseStack pose, MultiBufferSource bufferSource) {
		if (state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL) {
			BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

			blockRenderer.getModelRenderer().tesselateBlock(fullbrightLevel, blockRenderer.getBlockModel(state), state, BlockPos.ZERO, pose, bufferSource.getBuffer(ItemBlockRenderTypes.getChunkRenderType(state)), false, Minecraft.getInstance().level.random, 0, OverlayTexture.NO_OVERLAY);
		}
	}

	private void updateFromStack(ItemStack stack) {
		Item item = stack.getItem();

		if (item instanceof BlockItem blockItem) {
			block = blockItem.getBlock();
			state = block.defaultBlockState();
		}
		else {
			block = Blocks.AIR;
			state = Blocks.AIR.defaultBlockState();
		}

		updateBlockEntityInfo(true, true);
	}

	private void updateBlockEntityInfo(boolean resetPage, boolean ValidateStandingOrWall) {
		if (ValidateStandingOrWall && block.asItem() == Items.AIR) {
			block = Blocks.AIR;
			state = Blocks.AIR.defaultBlockState();
		}

		if (state.hasBlockEntity()) {
			Minecraft mc = Minecraft.getInstance();

			be = ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, state);
			renderDelegate.putDelegateFor(be, state);
		}
		else {
			if (be != null)
				renderDelegate.removeDelegateOf(be);

			be = null;
		}

		if (resetPage) {
			amountOfPages = Math.max(1, (int) Math.ceil(state.getProperties().size() / (float) PAGE_LENGTH));
			page = 1;
		}
	}

	public void changePage(int direction) {
		page += direction;

		if (page < 1)
			page = 1;
		else if (page > amountOfPages)
			page = amountOfPages;

		initButtons(false, false);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0 && mouseX >= xStart + 9 && mouseX <= xStart + 67 && mouseY >= yStart + 35 && mouseY <= yStart + 93) {
			this.dragX += (float) dragX;
			this.dragY += (float) dragY;
			return true;
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (previousPageButton.active && scroll > 0) {
			changePage(-1);
			return true;
		}
		else if (nextPageButton.active && scroll < 0) {
			changePage(1);
			return true;
		}

		return false;
	}

	@Override
	public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
		if (dataSlotIndex == slotToCheck) {
			updateFromStack(stack);

			if (previousPageButton != null)
				initButtons(true, true);

			menu.onStateChange(state);
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu menu, int slotIndex, int value) {}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {}

	public BlockState getState() {
		return state;
	}

	public StandingOrWallType getStandingOrWallType() {
		return standingOrWallType;
	}

	public List<Rect2i> getGuiExtraAreas() {
		return extraAreas;
	}

	public class BlockStatePropertyButton<T extends Comparable<T>> extends ToggleComponentButton {
		private final Property<T> property;
		private T value;

		public BlockStatePropertyButton(int xPos, int yPos, int width, int height, int initialValue, Property<T> property) {
			super(xPos, yPos, width, height, null, initialValue, property.getPossibleValues().size(), b -> {});
			this.property = property;
			onValueChange();
		}

		@Override
		public void onValueChange() {
			if (property != null) {
				Collection<T> values = property.getPossibleValues();
				int i = 0;

				for (T t : values) {
					if (i++ == getCurrentIndex()) {
						value = t;
						break;
					}
				}

				setMessage(Component.literal(StateHolder.PROPERTY_ENTRY_TO_STRING_FUNCTION.apply(java.util.Map.entry(property, value)).substring(property.getName().length() + 1)));
			}
		}

		@Override
		public void onPress() {
			super.onPress();
			state = state.setValue(property, value);

			if (be != null)
				renderDelegate.putDelegateFor(be, state);

			menu.onStateChange(state);
		}

		public Property<T> getProperty() {
			return property;
		}

		public T getValue() {
			return value;
		}
	}
}
