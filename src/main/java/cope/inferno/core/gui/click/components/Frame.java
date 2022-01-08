package cope.inferno.core.gui.click.components;

import cope.inferno.core.features.module.client.ClickGUI;
import cope.inferno.core.gui.base.AbstractComponent;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;

public abstract class Frame extends AbstractComponent {
    public static final int BACKGROUND = new Color(32, 32, 32, 153).getRGB();
    public static final int HEADER = new Color(0, 0, 0, 204).getRGB();

    private boolean expanded = true;

    private double scrollY;

    private double dragX, dragY;
    private boolean dragging = false;

    public Frame(String name, double x, double y) {
        super(name);

        this.x = x;
        this.y = y;
        this.width = 105.0;
        this.height = 215.0;

        this.init();
    }

    public abstract void init();

    @Override
    public void render(int mouseX, int mouseY) {
        if (dragging) {
            x = dragX + mouseX;
            y = dragY + mouseY;
        }

        if (isMouseInBounds(mouseX, mouseY)) {
            double speed = ClickGUI.scrollSpeed.getValue();
            int scroll = Mouse.getDWheel();

            if (ClickGUI.scrollInvert.getValue()) {
                speed = -speed;
            }

            if (scroll > 0) {
                for (AbstractComponent child : children) {
                    child.setY(child.getY() - speed);
                }
            } else if (scroll < 0) {
                for (AbstractComponent child : children) {
                    child.setY(child.getY() + speed);
                }
            }
        }

        double headerHeight = 17.5;

        // draw header
        RenderUtil.drawRectangle(x, y, width, headerHeight, HEADER);
        RenderUtil.drawOutline(x, y, width, expanded ? height : headerHeight, 3.0f, new Color(20, 20, 20).getRGB());
        getInferno().getFontManager().drawNormalizedString(name, (float) ScaleUtil.alignV(name, x, width), (float) ScaleUtil.alignH(y, headerHeight), -1);

        // scissor elements so they are cleanly restricted within the frame
        RenderUtil.startScissor((int) x, (int) (y + headerHeight), (int) (x + width), (int) (y + height));

        // if opened, we'll render our drawer (?)
        if (expanded) {
            RenderUtil.drawRectangle(x, y + headerHeight, width, height - headerHeight, BACKGROUND);

            double startY = 0.0;
            if (children.get(0).getY() == 0.0) {
                startY = y + headerHeight + 1.0;
            } else {
                startY = children.get(0).getY();
            }

            for (AbstractComponent component : children) {
                component.setX(x + 2.0);
                component.setY(startY);
                component.setHeight(14.0);
                component.setWidth(width - 4.0);

                component.render(mouseX, mouseY);

                startY += component.getHeight() + 1.0;
            }
        }

        RenderUtil.stopScissor();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseWithinBounds(mouseX, mouseY, x, y, width, 12.0)) {
            playClickSound();

            switch (mouseButton) {
                case 0:
                    dragX = x - mouseX;
                    dragY = y - mouseY;

                    dragging = !dragging;
                    break;

                case 1:
                    expanded = !expanded;
                    break;
            }
        }

        children.forEach((child) -> child.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0 && dragging) {
            dragging = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        children.forEach((child) -> child.keyTyped(typedChar, keyCode));
    }
}
