package cope.inferno.impl.ui;

import cope.inferno.impl.features.module.modules.client.GUI;
import cope.inferno.impl.ui.click.ClickGUIComponent;
import cope.inferno.impl.ui.components.Component;
import cope.inferno.impl.ui.components.bar.BarRenderer;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class InfernoGUI extends GuiScreen {
    private static InfernoGUI INSTANCE;

    private final BarRenderer barRenderer;
    private Component current;

    private InfernoGUI() {
        this.barRenderer = new BarRenderer();
        this.current = ClickGUIComponent.getInstance();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        this.barRenderer.update();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.barRenderer.render(mouseX, mouseY);

        if (this.current != null) {
            this.current.render(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.barRenderer.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.current != null) {
            this.current.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.barRenderer.mouseReleased(mouseX, mouseY, state);

        if (this.current != null) {
            this.current.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.barRenderer.keyTyped(typedChar, keyCode);

        if (this.current != null) {
            this.current.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return GUI.pause.getValue();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (GUI.INSTANCE.isOn()) {
            GUI.INSTANCE.toggle();
        }
    }

    public void setCurrentComponent(Component component) {
        this.current = component;
    }

    public static InfernoGUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InfernoGUI();
        }

        return INSTANCE;
    }
}
