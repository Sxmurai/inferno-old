package cope.inferno.impl.newui.main;

import cope.inferno.impl.features.module.modules.client.GUI;
import cope.inferno.impl.newui.main.bar.BarRenderer;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class InfernoUI extends GuiScreen {
    private static InfernoUI INSTANCE;

    private final BarRenderer barRenderer;

    private InfernoUI() {
        this.barRenderer = new BarRenderer("bar", 0.0, 0.0);
    }

    @Override
    public void updateScreen() {
        this.barRenderer.onUpdate();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.barRenderer.onRender(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.barRenderer.onRender(mouseX, mouseY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return GUI.INSTANCE.isOn();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GUI.INSTANCE.toggle();
    }

    public static InfernoUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InfernoUI();
        }

        return INSTANCE;
    }
}
