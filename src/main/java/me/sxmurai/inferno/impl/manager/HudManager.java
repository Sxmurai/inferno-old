package me.sxmurai.inferno.impl.manager;

import me.sxmurai.inferno.impl.features.hud.HudComponent;
import me.sxmurai.inferno.impl.features.hud.components.Watermark;

import java.util.ArrayList;

public class HudManager {
    private final ArrayList<HudComponent> components = new ArrayList<>();

    public HudManager() {
        this.components.add(new Watermark());

        this.components.forEach(HudComponent::registerAllSettings);
    }

    public ArrayList<HudComponent> getComponents() {
        return components;
    }
}
