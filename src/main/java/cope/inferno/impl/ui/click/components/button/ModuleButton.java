package cope.inferno.impl.ui.click.components.button;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.ui.components.Component;
import cope.inferno.impl.ui.components.widgets.button.Button;
import cope.inferno.Inferno;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ScaleUtil;
import cope.inferno.impl.settings.Bind;
import cope.inferno.impl.ui.click.components.other.Slider;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class ModuleButton extends Button {
    private final static ResourceLocation THREE_DOTS_RESOURCE = new ResourceLocation("inferno", "textures/three_dots.png");

    private final Module module;
    private boolean expanded = false;

    private final ArrayList<Component> components = new ArrayList<>();

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.init();
    }

    private void init() {
        for (Setting setting : this.module.getSettings()) {
            if (setting instanceof Bind && setting.getName().equals("Bind")) {
                this.components.add(new BindButton(this.module));
                continue;
            }

            if (setting.getValue() instanceof Boolean) {
                this.components.add(new BooleanButton(setting));
            } else if (setting.getValue() instanceof Enum) {
                this.components.add(new EnumButton(setting));
            } else if (setting.getValue() instanceof Number) {
                if (!setting.isNumberSetting()) {
                    continue;
                }

                this.components.add(new Slider(setting));
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name, ((float) this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), this.module.isOn() ? -1 : -5592406);

        if (this.module.getSettings().size() > 2) { // has more than the two default already added settings
            int width = Inferno.fontManager.getWidth("...");
            Inferno.fontManager.drawCorrectString("...", (float) (this.x + this.width - 2.0) - width, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
        }

        if (this.expanded) {
            double posY = 1.0;
            for (Component component : this.components) {
                if (!component.isVisible()) {
                    continue;
                }

                component.setX(this.x + 2.0);
                component.setY(this.y + (posY += component.getHeight() + 1.0));
                component.setWidth(this.width - 2.0);
                component.setHeight(13.0);

                component.render(mouseX, mouseY);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (this.expanded) {
            this.components.forEach((component) -> component.mouseClicked(mouseX, mouseY, button));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (this.expanded) {
            this.components.forEach((component) -> component.mouseReleased(mouseX, mouseY, state));
        }
    }

    @Override
    public void keyTyped(char character, int code) {
        super.keyTyped(character, code);
        if (this.expanded) {
            this.components.forEach((component) -> component.keyTyped(character, code));
        }
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.module.toggle();
        } else if (button == 1) {
            this.expanded = !this.expanded;
        }
    }

    @Override
    public double getHeight() {
        double h = this.height;

        if (this.expanded) {
            for (Component component : this.components) {
                if (!component.isVisible()) {
                    continue;
                }

                h += component.getHeight() + 1.0;
            }
        }

        return h + 1.0;
    }

    public Module getModule() {
        return module;
    }
}
