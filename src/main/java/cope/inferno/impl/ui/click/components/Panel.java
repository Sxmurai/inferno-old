package cope.inferno.impl.ui.click.components;

import cope.inferno.impl.features.module.modules.client.Colors;
import cope.inferno.impl.ui.click.components.button.ModuleButton;
import cope.inferno.impl.ui.components.Component;
import cope.inferno.Inferno;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class Panel extends Component {
    protected final ArrayList<ModuleButton> buttons = new ArrayList<>();

    private boolean dragging = false;
    private double x2, y2;

    private boolean resizing = false;
    private double h2;

    public Panel(String name, double x, double y) {
        super(name);

        this.width = 94.0;
        this.height = 235.0;
        this.x = x;
        this.y = y;

        this.init();
    }

    protected void init() { }

    @Override
    public void render(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = this.x2 + mouseX;
            this.y = this.y2 + mouseY;
        }

        if (this.resizing) {
            this.height = Math.max(this.h2 + mouseY, this.y + 16.0);
        }

        if (this.isMouseInBounds(mouseX, mouseY)) {
            int scroll = Mouse.getDWheel();
            if (scroll < 0) {
                this.buttons.forEach((button) -> button.setY(button.getY() - 10.0));
            } else if (scroll > 0) {
                this.buttons.forEach((button) -> button.setY(button.getY() + 10.0));
            }
        }

        // title bar
        RenderUtil.drawHalfRoundedRectangle(this.x, this.y, this.width, 16.0, 15.0, new Color(35, 39, 42).getRGB());
        Inferno.fontManager.drawCorrectString(
                this.name,
                ScaleUtil.centerTextX((float) this.x, (float) this.width, mc.fontRenderer.getStringWidth(this.name)),
                ScaleUtil.centerTextY((float) this.y, 14.0f) + 2.5f,
                -1
        );

        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        RenderUtil.scissor((int) this.x, (int) (this.y) + 15, (int) (this.x + this.width), (int) (this.y + this.height));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        // rest
        RenderUtil.drawRoundedRectangle(this.x, this.y, this.width, this.height, 15.0, new Color(35, 39, 42).getRGB());
        RenderUtil.drawLine(this.x, this.y + 15.0, this.x + this.width, this.y + 15.0, 2.0f, Colors.color());

        double firstButtonY = this.buttons.get(0).getY();
        double buttonY = firstButtonY == 0.0 ? (this.y + 14.0) + 1.5 : firstButtonY;
        for (ModuleButton button : this.buttons) {
            button.setX(this.x + 2.0);
            button.setY(buttonY);
            button.setHeight(14.0);
            button.setWidth(this.width - 4.0);

            button.render(mouseX, mouseY);

            buttonY += button.getHeight() + 1.5;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            if (this.isMouseWithinBounds(mouseX, mouseY, this.x, this.y, this.width, 15.0)) {
                this.x2 = this.x - mouseX;
                this.y2 = this.y - mouseY;
                this.dragging = true;
            }

            if (this.isMouseWithinBounds(mouseX, mouseY, this.x, (this.y + this.height) - 5.0, this.width, 5.0)) {
                this.resizing = true;
                this.h2 = this.height - mouseY;
            }
        }

        this.buttons.forEach((b) -> b.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.dragging) {
            this.dragging = false;
        }

        if (this.resizing) {
            this.resizing = false;
        }

        this.buttons.forEach((b) -> b.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void keyTyped(char character, int code) {
        this.buttons.forEach((b) -> b.keyTyped(character, code));
    }

    private boolean isMouseWithinBounds(int mouseX, int mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public ArrayList<ModuleButton> getButtons() {
        return buttons;
    }
}
