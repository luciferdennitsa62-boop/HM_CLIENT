package com.client.impl.module;

/**
 * Базовый класс настройки модуля. Конкретные типы (BoolSetting, DoubleSetting)
 * наследуются от него — так ClickGUI может рендерить их единообразно,
 * проверяя instanceof.
 */
public abstract class Setting<T> {
	private final String name;
	private final String description;
	protected T value;

	public Setting(String name, String description, T defaultValue) {
		this.name = name;
		this.description = description;
		this.value = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public static class BoolSetting extends Setting<Boolean> {
		public BoolSetting(String name, String description, boolean defaultValue) {
			super(name, description, defaultValue);
		}

		public void toggle() {
			setValue(!getValue());
		}
	}

	public static class DoubleSetting extends Setting<Double> {
		private final double min;
		private final double max;
		private final double step;

		public DoubleSetting(String name, String description, double defaultValue, double min, double max, double step) {
			super(name, description, defaultValue);
			this.min = min;
			this.max = max;
			this.step = step;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

		public double getStep() {
			return step;
		}

		@Override
		public void setValue(Double value) {
			super.setValue(Math.max(min, Math.min(max, value)));
		}
	}

	public static class EnumSetting<E extends Enum<E>> extends Setting<E> {
		public EnumSetting(String name, String description, E defaultValue) {
			super(name, description, defaultValue);
		}

		public void cycle() {
			E[] values = getValue().getDeclaringClass().getEnumConstants();
			int next = (getValue().ordinal() + 1) % values.length;
			setValue(values[next]);
		}
	}
}
