package cope.inferno.impl.newui.main.bar.interactables;

import cope.inferno.Inferno;
import cope.inferno.impl.newui.components.interactables.Button;

public abstract class TextButton extends Button {
    public TextButton(String id) {
        super(id, -1.0, -1.0, -1.0, -1.0);
    }

    @Override
    public void onUpdate() {
        this.width = Inferno.fontManager.getWidth(this.id);
    }

    @Override
    public void init() {

    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.id, this.x, this.y, -1);
    }
}
