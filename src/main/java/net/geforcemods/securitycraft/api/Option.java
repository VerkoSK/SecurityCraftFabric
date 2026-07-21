package net.geforcemods.securitycraft.api;

import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A class that allows blocks that have {@link ICustomizable} block entities to have custom, per-block options that are
 * separate from the main SecurityCraft configuration options.
 *
 * @author Geforce
 * @param <T> The Class of the type of value this option should use
 */
public abstract class Option<T> {
	private String name;
	protected T value;
	private T defaultValue;
	private T increment;
	private T minimum;
	private T maximum;

	protected Option(String optionName, T value) {
		this.name = optionName;
		this.value = value;
		this.defaultValue = value;
	}

	protected Option(String optionName, T value, T min, T max, T increment) {
		this.name = optionName;
		this.value = value;
		this.defaultValue = value;
		this.increment = increment;
		this.minimum = min;
		this.maximum = max;
	}

	public abstract void toggle();

	public abstract void load(ValueInput input);

	public abstract void save(ValueOutput output, T value);

	public void save(ValueOutput output) {
		save(output, value);
	}

	@SuppressWarnings("unchecked")
	public void copy(Option<?> option) {
		value = (T) option.get();
	}

	public String getName() {
		return name;
	}

	public T get() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public T getIncrement() {
		return increment;
	}

	public T getMin() {
		return minimum;
	}

	public T getMax() {
		return maximum;
	}

	public boolean isSlider() {
		return false;
	}

	public String getKey(String denotation) {
		return "option." + denotation + "." + getName();
	}

	public String getDescriptionKey(String denotation) {
		return getKey(denotation) + ".description";
	}

	public Component getDefaultInfo() {
		return Component.translatable("securitycraft.option.default_with_range", getDefaultValue(), getMin(), getMax()).withStyle(ChatFormatting.GRAY);
	}

	public Component getValueText() {
		return Component.literal(toString());
	}

	@Override
	public String toString() {
		return (value) + "";
	}

	public EntityDataWrappedOption<T> wrapForEntityData(EntityDataAccessor<T> entityDataKey, Supplier<SynchedEntityData> entityData) {
		return new EntityDataWrappedOption<>(this, entityDataKey, entityData);
	}

	/**
	 * A subclass of {@link Option}, set up to handle booleans.
	 */
	public static class BooleanOption extends Option<Boolean> {
		public BooleanOption(String optionName, Boolean value) {
			super(optionName, value);
		}

		@Override
		public void toggle() {
			setValue(!get());
		}

		@Override
		public void load(ValueInput input) {
			value = input.getBooleanOr(getName(), getDefaultValue());
		}

		@Override
		public void save(ValueOutput output, Boolean value) {
			output.putBoolean(getName(), value);
		}

		@Override
		public Component getDefaultInfo() {
			return Component.translatable("securitycraft.option.default", Component.translatable(getDefaultValue() ? "gui.securitycraft:invScan.yes" : "gui.securitycraft:invScan.no")).withStyle(ChatFormatting.GRAY);
		}

		@Override
		public Component getValueText() {
			return Component.translatable(get() ? "gui.securitycraft:invScan.yes" : "gui.securitycraft:invScan.no");
		}
	}

	public static class DisabledOption extends BooleanOption {
		public DisabledOption(Boolean value) {
			super("disabled", value);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.disabled";
		}
	}

	public static class IgnoreOwnerOption extends BooleanOption {
		public IgnoreOwnerOption(Boolean value) {
			super("ignoreOwner", value);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.ignoreOwner";
		}
	}

	public static class RespectInvisibilityOption extends BooleanOption {
		public RespectInvisibilityOption() {
			this(false);
		}

		public RespectInvisibilityOption(Boolean value) {
			super("respectInvisibility", value);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.respectInvisibility";
		}

		public boolean isConsideredInvisible(LivingEntity entity) {
			return get() && entity.hasEffect(MobEffects.INVISIBILITY);
		}
	}

	public static class SendAllowlistMessageOption extends BooleanOption {
		public SendAllowlistMessageOption(Boolean value) {
			super("sendAllowlistMessage", value);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.sendAllowlistMessage";
		}
	}

	public static class SendDenylistMessageOption extends BooleanOption {
		public SendDenylistMessageOption(Boolean value) {
			super("sendDenylistMessage", value);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.sendDenylistMessage";
		}
	}

	/**
	 * A subclass of {@link Option}, set up to handle integers.
	 */
	public static class IntOption extends Option<Integer> {
		public IntOption(String optionName, Integer value, Integer min, Integer max, Integer increment) {
			super(optionName, value, min, max, increment);
		}

		@Override
		public void toggle() {}

		@Override
		public void load(ValueInput input) {
			value = input.getIntOr(getName(), getDefaultValue());
		}

		@Override
		public void save(ValueOutput output, Integer value) {
			output.putInt(getName(), value);
		}

		@Override
		public boolean isSlider() {
			return true;
		}
	}

	public static class SmartModuleCooldownOption extends IntOption {
		public SmartModuleCooldownOption() {
			super("smartModuleCooldown", 100, 20, 400, 1);
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.smartModuleCooldown";
		}
	}

	public static class SignalLengthOption extends IntOption {
		public SignalLengthOption(int defaultLength) {
			super("signalLength", defaultLength, 0, 400, 5); //20 seconds max
		}

		@Override
		public String getKey(String denotation) {
			return "option.generic.signalLength";
		}
	}

	/**
	 * A subclass of {@link Option}, set up to handle doubles.
	 */
	public static class DoubleOption extends Option<Double> {
		public DoubleOption(String optionName, Double value, Double min, Double max, Double increment) {
			super(optionName, value, min, max, increment);
		}

		@Override
		public void toggle() {}

		@Override
		public void load(ValueInput input) {
			value = input.getDoubleOr(getName(), getDefaultValue());
		}

		@Override
		public void save(ValueOutput output, Double value) {
			output.putDouble(getName(), value);
		}

		@Override
		public String toString() {
			return Double.toString(value).length() > 5 ? Double.toString(value).substring(0, 5) : Double.toString(value);
		}

		@Override
		public boolean isSlider() {
			return true;
		}
	}

	public static class EnumOption<E extends Enum<E>> extends Option<E> {
		private final Class<E> enumClass;

		public EnumOption(String optionName, E value, Class<E> enumClass) {
			super(optionName, value);
			this.enumClass = enumClass;
		}

		@Override
		public void toggle() {
			E[] enumConstants = enumClass.getEnumConstants();
			int next = (value.ordinal() + 1) % enumConstants.length;

			value = enumConstants[next];
		}

		@Override
		public void load(ValueInput input) {
			E[] enumConstants = enumClass.getEnumConstants();
			int ordinal = input.getIntOr(getName(), -1);

			if (ordinal >= 0 && ordinal < enumConstants.length)
				value = enumConstants[ordinal];
			else
				value = getDefaultValue();
		}

		@Override
		public void save(ValueOutput output, E value) {
			output.putInt(getName(), value.ordinal());
		}

		@Override
		public Component getValueText() {
			return Component.literal(value.name());
		}

		@Override
		public Component getDefaultInfo() {
			return Component.translatable("securitycraft.option.default", getValueText()).withStyle(ChatFormatting.GRAY);
		}
	}

	public static class EntityDataWrappedOption<T> extends Option<T> {
		private final Option<T> wrapped;
		private final EntityDataAccessor<T> entityDataKey;
		private final Supplier<SynchedEntityData> entityData;

		public EntityDataWrappedOption(Option<T> wrapped, EntityDataAccessor<T> entityDataKey, Supplier<SynchedEntityData> entityData) {
			super(wrapped.getName(), wrapped.getDefaultValue());
			this.wrapped = wrapped;
			this.entityDataKey = entityDataKey;
			this.entityData = entityData;
		}

		@Override
		public void toggle() {
			wrapped.toggle();
		}

		@Override
		public void load(ValueInput input) {
			wrapped.load(input);
			entityData.get().set(entityDataKey, wrapped.get());
		}

		@Override
		public void save(ValueOutput output, T value) {
			wrapped.save(output, value);
		}

		@Override
		public void save(ValueOutput output) {
			wrapped.save(output, entityData.get().get(entityDataKey));
		}

		@Override
		public void copy(Option<?> option) {
			wrapped.copy(option);
		}

		@Override
		public final String getName() {
			return wrapped.getName();
		}

		@Override
		public T get() {
			return wrapped.get();
		}

		@Override
		public void setValue(T value) {
			wrapped.setValue(value);
			entityData.get().set(entityDataKey, wrapped.get());
		}

		@Override
		public T getDefaultValue() {
			return wrapped.getDefaultValue();
		}

		@Override
		public T getIncrement() {
			return wrapped.getIncrement();
		}

		@Override
		public T getMin() {
			return wrapped.getMin();
		}

		@Override
		public T getMax() {
			return wrapped.getMax();
		}

		@Override
		public boolean isSlider() {
			return wrapped.isSlider();
		}

		@Override
		public String getKey(String denotation) {
			return wrapped.getKey(denotation);
		}

		@Override
		public String getDescriptionKey(String denotation) {
			return wrapped.getDescriptionKey(denotation);
		}

		@Override
		public Component getDefaultInfo() {
			return wrapped.getDefaultInfo();
		}

		@Override
		public Component getValueText() {
			return wrapped.getValueText();
		}

		@Override
		public String toString() {
			return wrapped.toString();
		}

		public Option<T> getWrapped() {
			return wrapped;
		}

		public EntityDataAccessor<T> getEntityDataKey() {
			return entityDataKey;
		}
	}
}
