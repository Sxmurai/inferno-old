package cope.inferno.impl.newui.components.frames;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.modules.client.Colors;
import cope.inferno.impl.newui.components.AbstractComponent;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.render.ScaleUtil;
import org.lwjgl.opengl.GL11;

public abstract class DefaultFrame extends AbstractFrame {
    private static final double RADIUS = 12.0;

    private boolean resizing;
    private double h2;

    public DefaultFrame(String id, double x, double y, double width, double height) {
        super(id, x, y, width, height);
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        super.onRender(mouseX, mouseY);
        if (this.resizing) {
            this.height = Math.max(this.h2 + mouseY, this.y + BAR_HEIGHT);
        }

        RenderUtil.drawHalfRoundedRectangle(this.x, this.y, this.width, BAR_HEIGHT, RADIUS, BACKGROUND_COLOR);
        Inferno.fontManager.drawCorrectString(this.id, ScaleUtil.alignV(this.id, this.x, this.width), ScaleUtil.alignH(this.y, 14.0) + 2.5, -1);

        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        RenderUtil.scissor((int) this.x, (int) (this.y) + 15, (int) (this.x + this.width), (int) (this.y + this.height));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        RenderUtil.drawRoundedRectangle(this.x, this.y, this.width, this.height, RADIUS, BACKGROUND_COLOR);
        RenderUtil.drawLine(this.x, this.y + (BAR_HEIGHT - 1.0), this.x + width, this.y + (BAR_HEIGHT - 1.0), 2.0f, Colors.color());

        double origin = this.children.get(0).getY();
        double posY = origin == 0.0 ? this.y + (BAR_HEIGHT - 0.5) : origin;
        for (AbstractComponent component : this.children) {
            component.setX(this.x + 2.0);
            component.setY(posY);
            component.setHeight(14.0);
            component.setWidth(this.width - 4.0);

            component.onRender(mouseX, mouseY);

            posY += component.getHeight() + 1.5;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void onScroll(int offset, boolean state) {
        if (offset < 0) {
            this.children.forEach((child) -> child.setY(child.getY() - 10.0));
        } else if (offset > 0) {
            this.children.forEach((child) -> child.setY(child.getY() + 10.0));
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        if (this.isMouseWithinBounds(mouseX, mouseY, this.x, (this.y + this.height) - 5.0, this.width, 5.0)) {
            this.resizing = true;
            this.h2 = this.height - mouseY;
        }

        this.children.forEach((child) -> child.onMouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        super.onMouseReleased(mouseX, mouseY, state);
        if (state == 0 && this.resizing) {
            this.resizing = false;
        }

        this.children.forEach((child) -> child.onMouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void onKeyTyped(char charTyped, int code) {
        this.children.forEach((child) -> child.onKeyTyped(charTyped, code));
    }
}
