package cope.inferno.core.setting;

import cope.inferno.util.internal.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;

public class Configurable implements Wrapper {
    protected final ArrayList<Setting> settings = new ArrayList<>();

    public void register() {
        Arrays.stream(getClass().getDeclaredFields())
                .filter((field) -> Setting.class.isAssignableFrom(field.getType()))
                .forEach((field) -> {
                    field.setAccessible(true);

                    try {
                        settings.add((Setting) field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    public ArrayList<Setting> getSettings() {
        return settings;
    }
}
