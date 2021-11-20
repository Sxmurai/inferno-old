package me.sxmurai.inferno.impl.ui.click.components.button;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.render.ScaleUtil;
import me.sxmurai.inferno.impl.settings.EnumConverter;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.impl.ui.components.widgets.button.Button;

public class EnumButton extends Button {
    private final Setting<Enum> setting;

    public EnumButton(Setting<Enum> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name + ": " + this.setting.getValue().name(), (float) (this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.setting.setValue(EnumConverter.increaseEnum(this.setting.getValue()));
        }
    }
}
