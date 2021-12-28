package cope.inferno.core.gui.base;

public abstract class AbstractButton extends AbstractComponent {
    public AbstractButton(String name) {
        super(name);
    }

    public abstract void onClick(int button);

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseInBounds(mouseX, mouseY)) {
            playClickSound();
            onClick(mouseButton);
        }
    }
}
