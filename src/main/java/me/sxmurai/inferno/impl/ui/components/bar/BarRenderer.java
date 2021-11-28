package me.sxmurai.inferno.impl.ui.components.bar;

import me.sxmurai.inferno.impl.ui.hud.HudEditorScreen;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import me.sxmurai.inferno.impl.ui.InfernoGUI;
import me.sxmurai.inferno.impl.ui.click.ClickGUIComponent;
import me.sxmurai.inferno.impl.ui.components.Component;
import me.sxmurai.inferno.impl.ui.components.bar.buttons.CustomTextButton;
import me.sxmurai.inferno.impl.ui.components.widgets.button.Button;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;

public class BarRenderer extends Component {
    private final ArrayList<Button> buttons = new ArrayList<>();

    public BarRenderer() {
        super("bar");

        this.buttons.add(new CustomTextButton("Modules") {
            @Override
            public void mouseClicked(int mouseX, int mouseY, int button) {
                super.mouseClicked(mouseX, mouseY, button);
                InfernoGUI.getInstance().setCurrentComponent(ClickGUIComponent.getInstance());
            }
        });

        this.buttons.add(new CustomTextButton("HUD") {
            @Override
            public void mouseClicked(int mouseX, int mouseY, int button) {
                super.mouseClicked(mouseX, mouseY, button);
                mc.displayGuiScreen(HudEditorScreen.getInstance());
            }
        });

        this.buttons.add(new CustomTextButton("Macros"));
        this.buttons.add(new CustomTextButton("Console"));
    }

    @Override
    public void update() {
        ScaledResolution resolution = new ScaledResolution(mc);

        this.width = resolution.getScaledWidth_double();
        this.height = 22.0;

        this.buttons.forEach(Component::update);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // draw the bar above
        RenderUtil.drawRectangle(0.0, 0.0, this.width, this.height, ColorUtil.getColor(0, 0, 0, 120));

        // draw the buttons in the middle
        double padding = 8.0;
        double posX = (this.width / 2.0) - this.buttons.get(0).getWidth() - padding - this.buttons.get(1).getWidth() - padding;
        for (Button button : this.buttons) {
            button.setX(posX);
            button.setY(6.0);
            posX += button.getWidth() + padding;
        }

        this.buttons.forEach((button) -> button.render(mouseX, mouseY));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.buttons.forEach((b) -> {
            if (b.isMouseInBounds(mouseX, mouseY)) {
                b.mouseClicked(mouseX, mouseY, button);
            }
        });
    }
}
