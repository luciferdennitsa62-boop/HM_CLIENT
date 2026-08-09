package com.client.impl.module;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    protected T value;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }

    public static final class BoolSetting extends Setting<Boolean> {
        public BoolSetting(String name, String description, boolean defaultValue) {
            super(name, description, defaultValue);
        }

        public void toggle() {
            value = !value;
        }
    }

    public static final class DoubleSetting extends Setting<Double> {
        private final double min;
        private final double max;
        private final double step;

        public DoubleSetting(String name, String description, double defaultValue,
                             double min, double max, double step) {
            super(name, description, clamp(defaultValue, min, max));
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public void setValue(Double value) {
            super.setValue(clamp(value, min, max));
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getStep() { return step; }
    }

    public static final class EnumSetting<E extends Enum<E>> extends Setting<E> {
        public EnumSetting(String name, String description, E defaultValue) {
            super(name, description, defaultValue);
        }

        public void cycle() {
            E[] values = value.getDeclaringClass().getEnumConstants();
            value = values[(value.ordinal() + 1) % values.length];
        }
    }
}
