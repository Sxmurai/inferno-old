package cope.inferno.core.setting;

import java.util.function.Supplier;

public class Setting<T> {
    private final String name;
    private final T defaultValue;

    private T value;

    private final Number min, max;

    private final Supplier<Boolean> visibility;
    private final Setting parent;

    public Setting(String name, T value) {
        this(null, name, value, null, null, null);
    }

    public Setting(Setting parent, String name, T value) {
        this(parent, name, value, null, null, null);
    }

    public Setting(String name, T value, Number min, Number max) {
        this(name, value, min, max, null);
    }

    public Setting(Setting parent, String name, T value, Number min, Number max) {
        this(parent, name, value, min, max, null);
    }

    public Setting(String name, T value, Number min, Number max, Supplier<Boolean> visibility) {
        this(null, name, value, min, max, visibility);
    }

    public Setting(Setting parent, String name, T value, Number min, Number max, Supplier<Boolean> visibility) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        this.defaultValue = value;
        this.min = min;
        this.max = max;
        this.visibility = visibility;
    }

    public Setting getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }

    public boolean isVisible() {
        return visibility == null || visibility.get();
    }
}
