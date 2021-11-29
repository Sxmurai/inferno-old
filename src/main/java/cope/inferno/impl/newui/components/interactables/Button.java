package cope.inferno.impl.newui.components.interactables;

import cope.inferno.impl.newui.components.AbstractComponent;

public abstract class Button extends AbstractComponent {
    public Button(String id, double x, double y, double width, double height) {
        super(id, x, y, width, height);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseInBounds(mouseX, mouseY)) {
            this.onClick(button);
            this.playClickSound();
        }
    }

    public abstract void onClick(int button);
}
