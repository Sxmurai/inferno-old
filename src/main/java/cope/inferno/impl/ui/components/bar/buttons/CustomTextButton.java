package cope.inferno.impl.ui.components.bar.buttons;

import cope.inferno.impl.ui.components.widgets.button.buttons.TextButton;
import cope.inferno.Inferno;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.timing.Timer;

public class CustomTextButton extends TextButton {
    private final Timer progressTimer = new Timer();
    private float progress = 0.0f;

    public CustomTextButton(String name) {
        super(name);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);

        int width = Inferno.fontManager.getWidth(this.name);
        if (this.progressTimer.passedMs(100L)) {
            this.progressTimer.reset();

            if (this.isMouseInBounds(mouseX, mouseY)) {
                if (progress < width + 2.0) {
                    progress += 1.0f;
                }
            } else {
                if (progress != 0.0f) {
                    progress -= 1.0f;
                }
            }
        }

        if (progress != 0.0f) {
            double posY = this.y + Inferno.fontManager.getHeight() + 1.5;
            RenderUtil.drawLine(this.x - 2.0, posY, this.x + this.progress, posY, 1.5f, -1);
        }
    }

    @Override
    public void update() {
        this.width = Inferno.fontManager.getWidth(this.name);
        this.height = 12.0;
    }
}
