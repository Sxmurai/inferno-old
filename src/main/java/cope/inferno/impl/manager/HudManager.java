package cope.inferno.impl.manager;

import cope.inferno.impl.features.hud.HudComponent;
import cope.inferno.impl.features.hud.components.Arraylist;
import cope.inferno.impl.features.hud.components.Speed;
import cope.inferno.impl.features.hud.components.Watermark;

import java.util.ArrayList;

public class HudManager {
    private final ArrayList<HudComponent> components = new ArrayList<>();

    public HudManager() {
        this.components.add(new Arraylist());
        this.components.add(new Speed());
        this.components.add(new Watermark());

        this.components.forEach(HudComponent::registerAllSettings);
    }

    public ArrayList<HudComponent> getComponents() {
        return components;
    }

    public <T extends HudComponent> T getComponent(String name) {
        for (HudComponent component : this.components) {
            if (component.getName().equalsIgnoreCase(name)) {
                return (T) component;
            }
        }

        return null;
    }
}
