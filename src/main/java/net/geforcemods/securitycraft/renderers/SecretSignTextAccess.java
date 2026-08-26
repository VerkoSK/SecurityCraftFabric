package net.geforcemods.securitycraft.renderers;

import java.lang.reflect.Field;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

/**
 * Reflection-backed access to {@link SignBlockEntity}'s private {@code frontText}/{@code backText} fields, used
 * by {@link SecretSignRenderer} and {@link SecretHangingSignRenderer} to blank a side's text for a single frame
 * without going through {@link SignBlockEntity#setText}, which carries edit-side effects not wanted for a
 * transient render-time swap.
 */
final class SecretSignTextAccess {
	private static final Field FRONT_TEXT = findField("frontText");
	private static final Field BACK_TEXT = findField("backText");

	private SecretSignTextAccess() {
	}

	private static Field findField(String name) {
		try {
			Field field = SignBlockEntity.class.getDeclaredField(name);

			field.setAccessible(true);
			return field;
		}
		catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	static void setFrontText(SignBlockEntity be, SignText text) {
		set(FRONT_TEXT, be, text);
	}

	static void setBackText(SignBlockEntity be, SignText text) {
		set(BACK_TEXT, be, text);
	}

	private static void set(Field field, SignBlockEntity be, SignText text) {
		try {
			field.set(be, text);
		}
		catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
