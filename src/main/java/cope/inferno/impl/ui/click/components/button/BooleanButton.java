package cope.inferno.impl.ui.click.components.button;

import cope.inferno.impl.ui.components.widgets.button.Button;
import cope.inferno.Inferno;
import cope.inferno.util.render.ScaleUtil;
import cope.inferno.impl.settings.Setting;

public class BooleanButton extends Button {
    private final Setting<Boolean> setting;

    public BooleanButton(Setting<Boolean> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name, (float) (this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), this.setting.getValue() ? -1 : -5592406);
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.setting.setValue(!this.setting.getValue());
        }
    }

    @Override
    public boolean isVisible() {
        return this.setting.isVisible();
    }
}
