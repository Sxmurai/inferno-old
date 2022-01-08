package cope.inferno.core.gui.click.components.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.gui.base.AbstractButton;
import cope.inferno.core.gui.base.AbstractComponent;
import cope.inferno.core.gui.click.components.buttons.BindButton;
import cope.inferno.core.gui.click.components.buttons.BooleanButton;
import cope.inferno.core.gui.click.components.buttons.EnumButton;
import cope.inferno.core.setting.Bind;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import cope.inferno.util.text.FormatUtil;

import java.util.List;

import static cope.inferno.core.gui.click.components.buttons.ModuleButton.BACKGROUND;

public class DropdownMenu extends AbstractButton {
    private final Setting setting;

    private boolean expanded = false;

    public DropdownMenu(Setting setting) {
        super(setting.getName());

        this.setting = setting;

        ((List<Setting>) setting.getChildren()).forEach((child) -> {
            if (child.isParent()) {
                this.children.add(new DropdownMenu(child));
            } else if (setting instanceof Bind) {
                this.children.add(new BindButton((Bind) setting));
            } else {
                if (child.getValue() instanceof Boolean) {
                    this.children.add(new BooleanButton(child));
                } else if (child.getValue() instanceof Enum) {
                    this.children.add(new EnumButton(child));
                } else if (child.getValue() instanceof Number) {
                    this.children.add(new Slider(child));
                }
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY) {
        int color = -1;
        if (setting.getValue() instanceof Boolean) {
            color = ((Setting<Boolean>) setting).getValue() ? BACKGROUND.darker().darker().getRGB() : BACKGROUND.getRGB();
        } else {
            color = BACKGROUND.getRGB();
        }

        RenderUtil.drawRectangle(x, y, width, height, color);

        String text = name;
        if (setting.getValue() instanceof Enum) {
            Setting<Enum> enumSetting = (Setting<Enum>) setting;
            text += (": " + ChatFormatting.GRAY + FormatUtil.formatName(enumSetting.getValue().name()));
        }

        getInferno().getFontManager().drawNormalizedString(text, (float) (x + 3.0), (float) ScaleUtil.alignH(y, height), -1);

        int stringWidth = mc.fontRenderer.getStringWidth("...");
        getInferno().getFontManager().drawNormalizedString("...", (float) (((x + width) - stringWidth) - 1.0), (float) ScaleUtil.alignH(y, height), -1);

        if (expanded) {
            double startY = y + height + 1.0;
            for (AbstractComponent child : children) {
                if (child.isVisible()) {
                    child.setX(x + 2.0);
                    child.setY(startY);
                    child.setHeight(14.0);
                    child.setWidth(width - 4.0);

                    child.render(mouseX, mouseY);

                    startY += child.getHeight() + 1.0;
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (expanded) {
            children.stream().filter(AbstractComponent::isVisible).forEach((child) -> child.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (expanded) {
            children.stream().filter(AbstractComponent::isVisible).forEach((child) -> child.mouseReleased(mouseX, mouseY, state));
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            children.stream().filter(AbstractComponent::isVisible).forEach((child) -> child.keyTyped(typedChar, keyCode));
        }
    }

    @Override
    public void onClick(int button) {
        switch (button) {
            case 0: {
                if (setting.getValue() instanceof Boolean) {
                    Setting<Boolean> boolSetting = (Setting<Boolean>) setting;
                    boolSetting.setValue(!boolSetting.getValue());
                } else if (setting.getValue() instanceof Enum) {
                    Setting<Enum> enumSetting = (Setting<Enum>) setting;
                    enumSetting.setValue(Setting.increase(enumSetting.getValue()));
                }
                break;
            }

            case 1: {
                expanded = !expanded;
            }
        }
    }

    @Override
    public double getHeight() {
        double h = height;

        if (expanded) {
            for (AbstractComponent component : children) {
                if (component.isVisible()) {
                    h += component.getHeight() + 1.0;
                }
            }
        }

        return h;
    }
}
