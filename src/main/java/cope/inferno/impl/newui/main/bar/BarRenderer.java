package cope.inferno.impl.newui.main.bar;

import cope.inferno.impl.newui.components.AbstractComponent;
import cope.inferno.impl.newui.main.bar.interactables.TextButton;
import cope.inferno.impl.newui.main.clickgui.ClickGuiComponent;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class BarRenderer extends AbstractComponent {
    private static final double PADDING = 8.0;

    private AbstractComponent currentComponent = ClickGuiComponent.getInstance();

    public BarRenderer(String id, double x, double y) {
        super(id, x, y, -1.0, -1.0);
        this.init();
    }

    @Override
    public void init() {
        this.children.add(new TextButton("Modules") {
            @Override
            public void onClick(int button) {
                currentComponent = ClickGuiComponent.getInstance();
            }
        });

        this.children.add(new TextButton("HUD") {
            @Override
            public void onClick(int button) {

            }
        });

        this.children.add(new TextButton("Macros") {
            @Override
            public void onClick(int button) {

            }
        });

        this.children.add(new TextButton("Console") {
            @Override
            public void onClick(int button) {

            }
        });
    }

    @Override
    public void onUpdate() {
        ScaledResolution resolution = new ScaledResolution(mc);

        this.width = resolution.getScaledWidth_double();
        this.height = 22.0;

        this.children.forEach(AbstractComponent::onUpdate);
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        RenderUtil.drawRectangle(this.x, this.y, this.width, this.height, ColorUtil.getColor(0, 0, 0, 120));

        double posX = (this.width / 2.0) - this.children.get(0).getWidth() - PADDING - this.children.get(1).getWidth() - PADDING;
        for (AbstractComponent component : this.children) {
            component.setX(posX);
            component.setY(6.0);

            posX += component.getWidth() + PADDING;

            component.onRender(mouseX, mouseY);
        }

        if (this.currentComponent != null) {
            this.currentComponent.onRender(mouseX, mouseY);
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        this.children.forEach((child) -> {
            if (child.isMouseInBounds(mouseX, mouseY)) {
                child.onMouseClicked(mouseX, mouseY, button);
            }
        });

        if (this.currentComponent != null) {
            this.currentComponent.onMouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        if (this.currentComponent != null) {
            this.currentComponent.onMouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void onKeyTyped(char charTyped, int code) {
        if (this.currentComponent != null) {
            this.currentComponent.onKeyTyped(charTyped, code);
        }
    }
}
