package me.sxmurai.inferno.impl.ui.click.components.other;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.render.RenderUtil;
import me.sxmurai.inferno.util.render.ScaleUtil;
import me.sxmurai.inferno.impl.option.Option;
import me.sxmurai.inferno.impl.ui.components.Component;
import org.lwjgl.input.Mouse;

import java.awt.*;

// @todo the bar used for setting a value is a bit off with rendering, fix that kthx
public class Slider extends Component {
    private final Option<Number> option;
    private final float difference;

    public Slider(Option<Number> option) {
        super(option.getName());
        this.option = option;
        this.difference = this.option.getMax().floatValue() - this.option.getMin().floatValue();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        if (this.canSetValue(mouseX, mouseY)) {
            this.setValue(mouseX);
        }

        float center = ScaleUtil.centerTextY((float) this.y, (float) this.height);

        Inferno.fontManager.drawCorrectString(this.name + ": " + this.option.getValue(), (float) (this.x) + 2.3f, center - 2.0f, -1);

        double endX = this.x + (this.option.getValue().floatValue() <= this.option.getMin().floatValue() ? 0.0 : this.width * this.partialMultiplier());
        double posY = this.y + Inferno.fontManager.getHeight() + 2.0;

        RenderUtil.drawLine(this.x + 2.3, posY, endX - 1.0, posY,1.55f, new Color(253, 31, 31).getRGB());
        RenderUtil.drawCircle(endX, posY, 1.55, new Color(253, 31, 31).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (this.canSetValue(mouseX, mouseY)) {
            this.setValue(mouseX);
        }
    }

    private void setValue(int mouseX) {
        float percent = (float) (((double) (mouseX) - this.x) / (float) this.width);

        if (this.option.getValue() instanceof Float) {
            float result = this.option.getMin().floatValue() + this.difference * percent;
            this.option.setValue(Math.round(10.0f * result) / 10.0f);
        } else if (this.option.getValue() instanceof Double) {
            double result = this.option.getMin().doubleValue() + this.difference * percent;
            this.option.setValue(Math.round(10.0 * result) / 10.0);
        } else {
            this.option.setValue(Math.round(this.option.getMin().intValue() + this.difference * percent));
        }
    }

    private float part() {
        return this.option.getValue().floatValue() - this.option.getMin().floatValue();
    }

    private float partialMultiplier() {
        return this.part() / this.difference;
    }

    private boolean canSetValue(int mouseX, int mouseY) {
        return this.isMouseInBounds(mouseX, mouseY) && Mouse.isButtonDown(0); // 0 = left click
    }
}
