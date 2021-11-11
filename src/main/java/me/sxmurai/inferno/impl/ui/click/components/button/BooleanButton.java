package me.sxmurai.inferno.impl.ui.click.components.button;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.render.ScaleUtil;
import me.sxmurai.inferno.impl.option.Option;
import me.sxmurai.inferno.impl.ui.components.widgets.button.Button;

public class BooleanButton extends Button {
    private final Option<Boolean> option;

    public BooleanButton(Option<Boolean> option) {
        super(option.getName());
        this.option = option;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name, (float) (this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), this.option.getValue() ? -1 : -5592406);
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.option.setValue(!this.option.getValue());
        }
    }

    @Override
    public boolean isVisible() {
        return this.option.isVisible();
    }
}
