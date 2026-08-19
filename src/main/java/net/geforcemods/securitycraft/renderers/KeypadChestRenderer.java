package net.geforcemods.securitycraft.renderers;

import java.util.Calendar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Draws the keypad chest with its own artwork, and swaps to the "active" set once the lid is fully open.
 *
 * <p>Upstream only overrides {@code ChestRenderer#getMaterial}, a hook Forge patches in. Vanilla picks the
 * texture inline through {@link Sheets#chooseMaterial}, so this renderer does what vanilla's does and chooses the
 * material itself; everything else is vanilla's chest rendering, unchanged.
 */
public class KeypadChestRenderer implements BlockEntityRenderer<KeypadChestBlockEntity> {
	private static final Material ACTIVE = createMaterial("active");
	private static final Material INACTIVE = createMaterial("inactive");
	private static final Material LEFT_ACTIVE = createMaterial("left_active");
	private static final Material LEFT_INACTIVE = createMaterial("left_inactive");
	private static final Material RIGHT_ACTIVE = createMaterial("right_active");
	private static final Material RIGHT_INACTIVE = createMaterial("right_inactive");
	private static final Material CHRISTMAS = createMaterial("christmas");
	private static final Material CHRISTMAS_LEFT = createMaterial("christmas_left");
	private static final Material CHRISTMAS_RIGHT = createMaterial("christmas_right");
	private final ModelPart bottom;
	private final ModelPart lid;
	private final ModelPart lock;
	private final ModelPart doubleLeftBottom;
	private final ModelPart doubleLeftLid;
	private final ModelPart doubleLeftLock;
	private final ModelPart doubleRightBottom;
	private final ModelPart doubleRightLid;
	private final ModelPart doubleRightLock;
	private final boolean isChristmas;

	public KeypadChestRenderer(BlockEntityRendererProvider.Context ctx) {
		Calendar calendar = Calendar.getInstance();

		isChristmas = calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;

		ModelPart single = ctx.bakeLayer(ModelLayers.CHEST);

		bottom = single.getChild("bottom");
		lid = single.getChild("lid");
		lock = single.getChild("lock");

		ModelPart left = ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);

		doubleLeftBottom = left.getChild("bottom");
		doubleLeftLid = left.getChild("lid");
		doubleLeftLock = left.getChild("lock");

		ModelPart right = ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);

		doubleRightBottom = right.getChild("bottom");
		doubleRightLid = right.getChild("lid");
		doubleRightLock = right.getChild("lock");
	}

	@Override
	public void render(KeypadChestBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Level level = be.getLevel();
		boolean hasLevel = level != null;
		BlockState state = hasLevel ? be.getBlockState() : SCContent.KEYPAD_CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType type = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		Block block = state.getBlock();

		if (!(block instanceof AbstractChestBlock<?> chestBlock))
			return;

		pose.pushPose();
		pose.translate(0.5F, 0.5F, 0.5F);
		pose.mulPose(Axis.YP.rotationDegrees(-state.getValue(ChestBlock.FACING).toYRot()));
		pose.translate(-0.5F, -0.5F, -0.5F);

		DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combiner = hasLevel ? chestBlock.combine(state, level, be.getBlockPos(), true) : DoubleBlockCombiner.Combiner::acceptNone;
		float openness = combiner.apply(ChestBlock.opennessCombiner(be)).get(partialTicks);

		openness = 1.0F - openness;
		openness = 1.0F - openness * openness * openness;

		int brightness = combiner.apply(new BrightnessCombiner<>()).applyAsInt(packedLight);
		VertexConsumer consumer = getMaterial(be, type).buffer(buffer, RenderType::entityCutout);

		if (type == ChestType.LEFT)
			renderChest(pose, consumer, doubleLeftLid, doubleLeftLock, doubleLeftBottom, openness, brightness, packedOverlay);
		else if (type == ChestType.RIGHT)
			renderChest(pose, consumer, doubleRightLid, doubleRightLock, doubleRightBottom, openness, brightness, packedOverlay);
		else
			renderChest(pose, consumer, lid, lock, bottom, openness, brightness, packedOverlay);

		pose.popPose();
	}

	private void renderChest(PoseStack pose, VertexConsumer consumer, ModelPart lidPart, ModelPart lockPart, ModelPart bottomPart, float openness, int packedLight, int packedOverlay) {
		lidPart.xRot = -(openness * ((float) Math.PI / 2F));
		lockPart.xRot = lidPart.xRot;
		lidPart.render(pose, consumer, packedLight, packedOverlay);
		lockPart.render(pose, consumer, packedLight, packedOverlay);
		bottomPart.render(pose, consumer, packedLight, packedOverlay);
	}

	private Material getMaterial(ChestBlockEntity be, ChestType type) {
		if (isChristmas)
			return getMaterialForType(type, CHRISTMAS_LEFT, CHRISTMAS_RIGHT, CHRISTMAS);
		else if (be.getOpenNess(0.0F) >= 0.9F)
			return getMaterialForType(type, LEFT_ACTIVE, RIGHT_ACTIVE, ACTIVE);
		else
			return getMaterialForType(type, LEFT_INACTIVE, RIGHT_INACTIVE, INACTIVE);
	}

	private Material getMaterialForType(ChestType type, Material left, Material right, Material single) {
		return switch (type) {
			case LEFT -> left;
			case RIGHT -> right;
			default -> single;
		};
	}

	private static Material createMaterial(String name) {
		return new Material(Sheets.CHEST_SHEET, SCContent.id("entity/chest/" + name));
	}
}
