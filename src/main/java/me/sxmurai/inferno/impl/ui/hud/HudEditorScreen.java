package me.sxmurai.inferno.impl.ui.hud;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.hud.HudComponent;
import me.sxmurai.inferno.impl.ui.hud.components.HudPanel;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class HudEditorScreen extends GuiScreen {
    private static HudEditorScreen INSTANCE;

    private final HudPanel panel;

    private HudComponent dragging = null;
    private double x2, y2;

    private HudEditorScreen() {
        this.panel = new HudPanel("Components", 4.0, 4.0) {
            @Override
            protected void init() {
                // @todo
            }
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        if (this.dragging != null) {
            this.dragging.setX(this.x2 + mouseX);
            this.dragging.setY(this.y2 + mouseY);
        }

        for (HudComponent component : Inferno.hudManager.getComponents()) {
            if (component.isVisible()) {
                RenderUtil.drawRoundedRectangle(component.getX(), component.getY(), component.getWidth(), component.getHeight(), 10.0, ColorUtil.getColor(255, 255, 255, 80));
                component.render();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (HudComponent component : Inferno.hudManager.getComponents()) {
                if (component.isMouseInBounds(mouseX, mouseY)) {
                    this.x2 = component.getX() - mouseX;
                    this.y2 = component.getY() - mouseY;
                    this.dragging = component;
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0 && this.dragging != null) {
            this.dragging = null;
        }
    }

    public static HudEditorScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HudEditorScreen();
        }

        return INSTANCE;
    }
}
