package me.sxmurai.inferno.impl.features.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.hud.HudComponent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.features.module.modules.client.Colors;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Arraylist extends HudComponent {
    public final Setting<Boolean> display = new Setting<>("Display", true);

    public Arraylist() {
        super("ArrayList");
        this.height = 100.0;
        this.setVisible(true);
    }

    private Quadrant quadrant = Quadrant.TopRight;

    @Override
    public void render() {
        this.setQuadrant();
        List<Module> modules = Inferno.moduleManager.getModules().stream().filter((module) -> module.isOn() && module.isDrawn()).collect(Collectors.toList());

        this.width = modules.stream().mapToDouble((module) -> Inferno.fontManager.getWidth(this.getFullDisplay(module))).max().orElse(0.0);

        modules.sort(Comparator.comparingInt((mod) -> {
            int width = Inferno.fontManager.getWidth(this.getFullDisplay(mod));
            return this.quadrant == Quadrant.TopRight || this.quadrant == Quadrant.TopLeft ? -width : width;
        }));

        double yOffset = 0.0;
        for (Module module : modules) {
            String text = this.getFullDisplay(module);
            Inferno.fontManager.drawCorrectString(text, this.x + (this.quadrant == Quadrant.TopRight || this.quadrant == Quadrant.BottomRight ? (this.width - Inferno.fontManager.getWidth(text)) : 1.0), this.y + yOffset, Colors.color());

            yOffset += Inferno.fontManager.getHeight() + 2.0;
        }

        this.height = yOffset;
    }

    private String getFullDisplay(Module module) {
        String display = module.getName();
        if (this.display.getValue() && module.getDisplayInfo() != null) {
            display += (ChatFormatting.GRAY +  " [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]");
        }

        return display;
    }

    private void setQuadrant() {
        ScaledResolution resolution = new ScaledResolution(mc);

        double x = this.x + this.width / 2.0;
        double y = this.y + this.height / 2.0;

        double w = resolution.getScaledWidth_double() / 2.0;
        double h = resolution.getScaledHeight_double() / 2.0;

        if (y >= h && x >= w) {
            this.quadrant = Quadrant.BottomRight;
        } else if (y >= h && x <= w) {
            this.quadrant = Quadrant.BottomLeft;
        } else if (y <= h && x >= w) {
            this.quadrant = Quadrant.TopRight;
        } else if (y <= h && x <= w) {
            this.quadrant = Quadrant.TopLeft;
        }
    }

    public enum Quadrant {
        TopRight, TopLeft,
        BottomRight, BottomLeft
    }
}
