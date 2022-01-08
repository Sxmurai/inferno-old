package cope.inferno.core.gui.click.components.buttons;

import cope.inferno.core.features.module.Module;
import cope.inferno.core.gui.base.AbstractButton;
import cope.inferno.core.gui.base.AbstractComponent;
import cope.inferno.core.gui.click.components.other.DropdownMenu;
import cope.inferno.core.gui.click.components.other.Slider;
import cope.inferno.core.setting.Bind;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;

import java.awt.*;

public class ModuleButton extends AbstractButton {
    public static final Color BACKGROUND = new Color(20, 19, 19, 208);

    private final Module module;

    private boolean expanded = false;

    public ModuleButton(Module module) {
        super(module.getName());

        this.module = module;

        module.getSettings().forEach((setting) -> {
            if (setting.isParent() && !setting.hasParent()) {
                this.children.add(new DropdownMenu(setting));
            } else if (setting instanceof Bind) {
                this.children.add(new BindButton((Bind) setting));
            } else {
                if (setting.hasParent() || setting.isParent()) {
                    return;
                }

                if (setting.getValue() instanceof Boolean) {
                    this.children.add(new BooleanButton(setting));
                } else if (setting.getValue() instanceof Enum) {
                    this.children.add(new EnumButton(setting));
                } else if (setting.getValue() instanceof Number) {
                    this.children.add(new Slider(setting));
                }
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY) {
        RenderUtil.drawRectangle(x, y, width, height, module.isToggled() ? BACKGROUND.darker().darker().getRGB() : BACKGROUND.getRGB());
        getInferno().getFontManager().drawNormalizedString(name, (float) (x + 3.0), (float) ScaleUtil.alignH(y, height), -1);

        if (module.getSettings().size() > 2) {
            int stringWidth = mc.fontRenderer.getStringWidth("...");
            getInferno().getFontManager().drawNormalizedString("...", (float) (((x + width) - stringWidth) - 1.0), (float) ScaleUtil.alignH(y, height), -1);
        }

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
                module.toggle();
                break;
            }

            case 1: {
                expanded = !expanded;
                break;
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
