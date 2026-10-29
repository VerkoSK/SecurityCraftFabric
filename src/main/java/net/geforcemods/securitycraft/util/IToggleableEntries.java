package net.geforcemods.securitycraft.util;

import java.util.Map;
import java.util.function.ToIntFunction;

public interface IToggleableEntries<T> {
	public void setFilter(T type, boolean allowed);

	public boolean getFilter(T type);

	public Map<T, Boolean> getFilters();

	public T getDefaultType();

	default ToIntFunction<T> getComparatorOutputFunction() {
		return t -> 0;
	}

	default String getDefaultTypeName() {
		return "gui.securitycraft:SmartModule.default";
	}
}
