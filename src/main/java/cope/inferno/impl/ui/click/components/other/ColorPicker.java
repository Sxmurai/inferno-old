package cope.inferno.impl.ui.click.components.other;

import cope.inferno.impl.settings.Setting;
import cope.inferno.impl.ui.components.Component;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;

// @todo fix
public class ColorPicker extends Component {
    private final Setting<ColorUtil.Color> setting;

    private double x2, y2;

    public ColorPicker(Setting<ColorUtil.Color> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        ColorUtil.Color color = this.setting.getValue();
        int start = ColorUtil.getColor((int) color.getRed(), (int) color.getGreen(), (int) color.getBlue(), (int) color.getAlpha());

        RenderUtil.drawGradientRectangle(this.x, this.y, this.width, 45.0, start, -1);

        if (this.canSet(mouseX, mouseY)) {
            this.set(mouseX, mouseY);
        }
    }

    private void set(int mouseX, int mouseY) {
        ColorUtil.Color color = this.setting.getValue();

        float s = (float) (((mouseX - this.x) - 1.0f) / this.width);
        float b = (float) (1.0f - ((mouseY - this.y) - 45.0f) / this.height);

        float[] hsb = Color.RGBtoHSB((int) color.getRed(), (int) color.getGreen(), (int) color.getBlue(), null);
        int hex = Color.HSBtoRGB(hsb[0], s, b);

        this.setting.setValue(new ColorUtil.Color((hex >> 16) & 0xff, (hex >> 8) & 0xff, hex & 0xff, color.getAlpha()));
    }

    private boolean canSet(int mouseX, int mouseY) {
        return Mouse.isButtonDown(0) && this.isMouseWithinBounds(mouseX, mouseY, this.x, this.y, this.width, 45.0);
    }

    private boolean isMouseWithinBounds(int mouseX, int mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public double getHeight() {
        return 75.0;
    }
}
