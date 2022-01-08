package cope.inferno.core.gui.click.components.buttons;

import cope.inferno.core.gui.base.AbstractButton;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;

import static cope.inferno.core.gui.click.components.buttons.ModuleButton.BACKGROUND;

public class BooleanButton extends AbstractButton {
    protected final Setting<Boolean> setting;

    public BooleanButton(Setting<Boolean> setting) {
        super(setting.getName());

        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        RenderUtil.drawRectangle(x, y, width, height, setting.getValue() ? BACKGROUND.darker().darker().getRGB() : BACKGROUND.getRGB());
        getInferno().getFontManager().drawNormalizedString(name, (float) (x + 3.0), (float) ScaleUtil.alignH(y, height), -1);
    }

    @Override
    public void onClick(int button) {
        if (button == 0) {
            setting.setValue(!setting.getValue());
        }
    }
}
