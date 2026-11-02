package net.geforcemods.securitycraft.mixin.compat.starhud;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.misc.ClientContainerLocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Compatibility mixin for StarHUD: displays the signature gold passcode text and color when looking at
 * SecurityCraft passcode blocks or modded containers locked via Key Panel (ContainerLockData).
 */
@Pseudo
@Mixin(targets = "fin.starhud.hud.implementation.other.TargetedCrosshairHUD", remap = false)
public class TargetedCrosshairHUDMixin {
	private static Field targetedNameColorField;
	private static Field cachedBlockNameField;
	private static Field cachedBlockMaxWidthField;
	private static Field widthField;
	private static Field heightField;
	private static Field cachedBlockField;
	private static Field blockStackField;
	private static Method setWidthHeightColorMethod;
	private static boolean reflectionInitialized;

	private static BlockPos lastTargetedPos;
	private static boolean wasCustomized;

	private static void initReflection() {
		if (reflectionInitialized)
			return;

		try {
			Class<?> clazz = Class.forName("fin.starhud.hud.implementation.other.TargetedCrosshairHUD");

			targetedNameColorField = clazz.getDeclaredField("targetedNameColor");
			targetedNameColorField.setAccessible(true);

			cachedBlockNameField = clazz.getDeclaredField("cachedBlockName");
			cachedBlockNameField.setAccessible(true);

			cachedBlockMaxWidthField = clazz.getDeclaredField("cachedBlockMaxWidth");
			cachedBlockMaxWidthField.setAccessible(true);

			widthField = clazz.getDeclaredField("width");
			widthField.setAccessible(true);

			heightField = clazz.getDeclaredField("height");
			heightField.setAccessible(true);

			cachedBlockField = clazz.getDeclaredField("cachedBlock");
			cachedBlockField.setAccessible(true);

			blockStackField = clazz.getDeclaredField("blockStack");
			blockStackField.setAccessible(true);

			Class<?> current = clazz;
			while (current != null && current != Object.class) {
				try {
					setWidthHeightColorMethod = current.getDeclaredMethod("setWidthHeightColor", int.class, int.class, int.class);
					setWidthHeightColorMethod.setAccessible(true);
					break;
				}
				catch (NoSuchMethodException e) {
					current = current.getSuperclass();
				}
			}
		}
		catch (Exception ignored) {}

		reflectionInitialized = true;
	}

	@Inject(method = "collectDataBlock", at = @At("HEAD"))
	private void securitycraft$onCollectDataBlockHead(CallbackInfoReturnable<Boolean> cir) {
		initReflection();

		Minecraft client = Minecraft.getInstance();

		if (client.hitResult instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hitResult.getBlockPos();

			if (!pos.equals(lastTargetedPos)) {
				if (wasCustomized && cachedBlockField != null) {
					try {
						cachedBlockField.set(this, null);
					}
					catch (Exception ignored) {}
					wasCustomized = false;
				}
				lastTargetedPos = pos.immutable();
			}
		}
		else if (lastTargetedPos != null) {
			if (wasCustomized && cachedBlockField != null) {
				try {
					cachedBlockField.set(this, null);
				}
				catch (Exception ignored) {}
				wasCustomized = false;
			}
			lastTargetedPos = null;
		}
	}

	@Inject(method = "collectDataBlock", at = @At("RETURN"))
	private void securitycraft$onCollectDataBlockReturn(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() || targetedNameColorField == null)
			return;

		Minecraft client = Minecraft.getInstance();

		if (client.hitResult instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hitResult.getBlockPos();
			Level level = client.level;

			if (level == null)
				return;

			boolean isModdedLocked = ClientContainerLocks.isLocked(pos);
			BlockEntity be = level.getBlockEntity(pos);
			boolean isPasscode = isModdedLocked || (be instanceof PasscodeProtected);

			if (isPasscode) {
				try {
					int goldColor = 0xFFFFAA00;
					targetedNameColorField.setInt(this, goldColor);

					int currentWidth = widthField != null ? widthField.getInt(this) : 0;
					int currentHeight = heightField != null ? heightField.getInt(this) : 22;

					if (isModdedLocked && cachedBlockNameField != null) {
						Component originalName = null;

						if (be instanceof Nameable nameable && nameable.hasCustomName())
							originalName = nameable.getCustomName();
						else if (blockStackField != null) {
							ItemStack stack = (ItemStack) blockStackField.get(this);
							if (stack != null && !stack.isEmpty())
								originalName = stack.getHoverName();
						}

						if (originalName == null)
							originalName = level.getBlockState(pos).getBlock().getName();

						Component prefix = Component.literal("[").append(Component.translatable("waila.securitycraft:passcodeProtected")).append(Component.literal("] ")).withStyle(ChatFormatting.GOLD);
						Component fullName = prefix.copy().append(originalName);

						FormattedCharSequence oldSeq = (FormattedCharSequence) cachedBlockNameField.get(this);
						int oldWidth = oldSeq != null ? client.font.width(oldSeq) : 0;
						FormattedCharSequence newSeq = fullName.getVisualOrderText();
						cachedBlockNameField.set(this, newSeq);
						int newWidth = client.font.width(newSeq);
						int delta = newWidth - oldWidth;

						if (delta > 0) {
							if (cachedBlockMaxWidthField != null) {
								int maxW = cachedBlockMaxWidthField.getInt(this);
								cachedBlockMaxWidthField.setInt(this, maxW + delta);
							}
							currentWidth += delta;
							if (widthField != null)
								widthField.setInt(this, currentWidth);
						}
					}

					if (setWidthHeightColorMethod != null)
						setWidthHeightColorMethod.invoke(this, currentWidth, currentHeight, goldColor);

					wasCustomized = true;
				}
				catch (Exception ignored) {}
			}
			else {
				wasCustomized = false;
			}
		}
	}
}
