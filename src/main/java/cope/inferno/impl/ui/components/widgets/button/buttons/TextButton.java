package cope.inferno.impl.ui.components.widgets.button.buttons;

import cope.inferno.Inferno;
import cope.inferno.impl.ui.components.widgets.button.Button;

public class TextButton extends Button {
    public TextButton(String name) {
        super(name);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Inferno.fontManager.drawCorrectString(this.name, this.x, this.y, -1);
    }
}
