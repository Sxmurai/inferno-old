package cope.inferno.core.gui.click.components.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.gui.base.AbstractButton;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import cope.inferno.util.text.FormatUtil;

import static cope.inferno.core.gui.click.components.buttons.ModuleButton.BACKGROUND;

public class EnumButton extends AbstractButton {
    private final Setting<Enum> setting;

    public EnumButton(Setting<Enum> setting) {
        super(setting.getName());

        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        RenderUtil.drawRectangle(x, y, width, height, BACKGROUND.getRGB());
        getInferno().getFontManager().drawNormalizedString(
                setting.getName() + ": " + ChatFormatting.GRAY + FormatUtil.formatName(setting.getValue().name()),
                (float) (x + 2.3),
                (float) ScaleUtil.alignH(y, height),
                -1
        );
    }

    @Override
    public void onClick(int button) {
        setting.setValue(Setting.increase(setting.getValue()));
    }
}
