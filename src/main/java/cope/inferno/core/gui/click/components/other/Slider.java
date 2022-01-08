package cope.inferno.core.gui.click.components.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.gui.base.AbstractComponent;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import org.lwjgl.input.Mouse;

import static cope.inferno.core.gui.click.components.buttons.ModuleButton.BACKGROUND;

public class Slider extends AbstractComponent {
    private final Setting<Number> setting;
    private final float difference;

    public Slider(Setting<Number> setting) {
        super(setting.getName());

        this.setting = setting;
        this.difference = setting.getMax().floatValue() - setting.getMin().floatValue();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        if (canSetValue(mouseX, mouseY)) {
            setValue(mouseX);
        }

        double w = setting.getValue().floatValue() <= setting.getMin().floatValue() ? 0.0 : width * partialMultiplier();
        RenderUtil.drawRectangle(x, y, w, height, BACKGROUND.darker().darker().getRGB());

        getInferno().getFontManager().drawNormalizedString(
                setting.getName() + ": " + ChatFormatting.GRAY + setting.getValue(),
                (float) (x + 2.3),
                (float) ScaleUtil.alignH(y, height),
                -1
        );
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (canSetValue(mouseX, mouseY)) {
            setValue(mouseX);
        }
    }

    private void setValue(int mouseX) {
        float percent = (float) (((double) (mouseX) - this.x) / (float) this.width);

        if (this.setting.getValue() instanceof Float) {
            float result = this.setting.getMin().floatValue() + this.difference * percent;
            this.setting.setValue(Math.round(10.0f * result) / 10.0f);
        } else if (this.setting.getValue() instanceof Double) {
            double result = this.setting.getMin().doubleValue() + this.difference * percent;
            this.setting.setValue(Math.round(10.0 * result) / 10.0);
        } else {
            this.setting.setValue(Math.round(this.setting.getMin().intValue() + this.difference * percent));
        }
    }

    private float part() {
        return this.setting.getValue().floatValue() - this.setting.getMin().floatValue();
    }

    private float partialMultiplier() {
        return this.part() / this.difference;
    }

    private boolean canSetValue(int mouseX, int mouseY) {
        return this.isMouseInBounds(mouseX, mouseY) && Mouse.isButtonDown(0); // 0 = left click
    }
}
