package me.sxmurai.inferno.impl.ui.click.components.button;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.render.ScaleUtil;
import me.sxmurai.inferno.impl.option.EnumConverter;
import me.sxmurai.inferno.impl.option.Option;
import me.sxmurai.inferno.impl.ui.components.widgets.button.Button;

public class EnumButton extends Button {
    private final Option<Enum> option;

    public EnumButton(Option<Enum> option) {
        super(option.getName());
        this.option = option;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name + ": " + this.option.getValue().name(), (float) (this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.option.setValue(EnumConverter.increaseEnum(this.option.getValue()));
        }
    }
}
