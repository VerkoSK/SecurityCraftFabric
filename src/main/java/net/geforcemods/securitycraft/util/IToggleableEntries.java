package net.geforcemods.securitycraft.util;

import java.util.Map;
import java.util.function.ToIntFunction;

public interface IToggleableEntries<T> {
	default void toggleFilter(T type) {
		setFilter(type, !getFilter(type));
	}

	void setFilter(T type, boolean allowed);

	public boolean getFilter(T type);

	Map<T, Boolean> getFilters();

	default ToIntFunction<T> getComparatorOutputFunction() {
		return t -> 0;
	}

	T getDefaultType();

	default String getDefaultTypeName() {
		return getDefaultType().toString();
	}
}
