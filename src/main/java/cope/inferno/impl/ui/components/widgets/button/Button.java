package cope.inferno.impl.ui.components.widgets.button;

import cope.inferno.impl.ui.components.Component;

public class Button extends Component {
    public Button(String name) {
        super(name);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseInBounds(mouseX, mouseY)) {
            this.playClickSound(1.0f);
            this.doAction(button);
        }
    }

    public void doAction(int button) { }
}
