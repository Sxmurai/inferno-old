package cope.inferno.impl.ui.click.components.button;

import cope.inferno.impl.ui.components.widgets.button.Button;
import cope.inferno.Inferno;
import cope.inferno.util.render.ScaleUtil;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;

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
