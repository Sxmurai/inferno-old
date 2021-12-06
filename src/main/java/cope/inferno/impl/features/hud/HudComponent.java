package cope.inferno.impl.features.hud;

import cope.inferno.impl.settings.Setting;
import cope.inferno.impl.ui.components.Component;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class HudComponent extends Component {
    private final ArrayList<Setting> settings = new ArrayList<>();
    private boolean visible = false;
    private final String description;

    public HudComponent(String name, String description) {
        super(name);
        this.description = description;
    }

    public abstract void render();

    public void registerAllSettings() {
        Arrays.stream(this.getClass().getDeclaredFields())
                .filter((field) -> Setting.class.isAssignableFrom(field.getType()))
                .forEach((field) -> {
                    try {
                        field.setAccessible(true);
                        this.settings.add((Setting) field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public String getDescription() {
        return description;
    }

    public Setting getSetting(String name) {
        for (Setting setting : this.settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public ArrayList<Setting> getSettings() {
        return settings;
    }
}
