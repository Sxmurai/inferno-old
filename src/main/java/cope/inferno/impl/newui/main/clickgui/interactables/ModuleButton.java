package cope.inferno.impl.newui.main.clickgui.interactables;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.newui.components.interactables.Button;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ScaleUtil;

public class ModuleButton extends Button {
    private final Module module;

    private boolean expanded = false;

    public ModuleButton(Module module) {
        super(module.getName(), 0.0, 0.0, 0.0, 0.0);
        this.module = module;
        this.init();
    }

    @Override
    public void init() {
        for (Setting setting : this.module.getSettings()) {
            // @todo
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.id, this.x + 2.3, ScaleUtil.alignH(this.y, this.height), this.module.isOn() ? -1 : -5592406);
        if (this.module.getSettings().size() > 2) {
            int width = Inferno.fontManager.getWidth("...");
            Inferno.fontManager.drawCorrectString("...", (this.x + this.width - 2.0) - width, ScaleUtil.alignH(this.y, this.height), -1);
        }


    }

    @Override
    public void onClick(int button) {
        if (button == 0) {
            this.module.toggle();
        } else if (button == 1) {
            this.expanded = !this.expanded;
        }
    }
}
