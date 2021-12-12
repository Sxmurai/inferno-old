package cope.inferno.impl.newui.main;

import cope.inferno.impl.features.module.modules.client.GUI;
import cope.inferno.impl.newui.components.AbstractComponent;
import cope.inferno.impl.newui.main.bar.BarRenderer;
import cope.inferno.impl.newui.main.clickgui.ClickGuiComponent;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class InfernoUI extends GuiScreen {
    private static InfernoUI INSTANCE;

    private final BarRenderer barRenderer;
    private AbstractComponent current;

    private InfernoUI() {
        this.barRenderer = new BarRenderer("bar", 0.0, 0.0);
        this.current = ClickGuiComponent.getInstance();
    }

    @Override
    public void updateScreen() {
        this.barRenderer.onUpdate();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.barRenderer.onRender(mouseX, mouseY);
        if (this.current != null) {
            this.current.onRender(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.barRenderer.onMouseClicked(mouseX, mouseY, mouseButton);
        if (this.current != null) {
            this.current.onMouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (this.current != null) {
            this.current.onMouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (this.current != null) {
            this.current.onKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return GUI.pause.getValue();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GUI.INSTANCE.toggle();
    }

    public void setCurrent(AbstractComponent current) {
        this.current = current;
    }

    public static InfernoUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InfernoUI();
        }

        return INSTANCE;
    }
}
