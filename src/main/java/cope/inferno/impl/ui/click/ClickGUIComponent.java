package cope.inferno.impl.ui.click;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.features.module.modules.client.GUI;
import cope.inferno.impl.ui.click.components.Panel;
import cope.inferno.impl.ui.click.components.button.ModuleButton;
import cope.inferno.impl.ui.components.Component;
import cope.inferno.Inferno;
import cope.inferno.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGUIComponent extends Component {
    public static ClickGUIComponent INSTANCE;
    private final ArrayList<cope.inferno.impl.ui.click.components.Panel> panels = new ArrayList<>();

    private ClickGUIComponent() {
        super("clickgui");

        double x = 4.0;
        for (Module.Category category : Module.Category.values()) {
            List<Module> modules = Inferno.moduleManager.getModules().stream().filter((module) -> module.getCategory().equals(category)).collect(Collectors.toList());
            if (modules.isEmpty()) {
                continue;
            }

            this.panels.add(new cope.inferno.impl.ui.click.components.Panel(category.name(), x, 25.0) {
                @Override
                protected void init() {
                    modules.forEach((module) -> this.buttons.add(new ModuleButton(module)));
                }
            });

            x += 98.0;
        }
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);
        this.panels.forEach((panel) -> panel.render(mouseX, mouseY));

        if (GUI.tooltips.getValue()) {
            for (Panel panel : this.panels) {
                for (ModuleButton button : panel.getButtons()) {
                    if (button.isMouseInBounds(mouseX, mouseY)) {
                        Module module = button.getModule();
                        this.renderTooltip(module, mouseX + 5.0, mouseY - 5.0);
                    }
                }
            }
        }
    }

    private void renderTooltip(Module module, double x, double y) {
        int screenWidth = new ScaledResolution(mc).getScaledWidth();
        String text = module.getDescription();

        double width = Inferno.fontManager.getWidth(text) + 4.0;
        if (x + width > screenWidth) {
            x = screenWidth - width;
        }

        RenderUtil.drawRoundedRectangle(x, y, width, Inferno.fontManager.getHeight() + 4.0, 10.0, new Color(35, 39, 42).darker().getRGB());
        Inferno.fontManager.drawCorrectString(text, x + 2.3, y + 2.0, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.panels.forEach((b) -> b.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.panels.forEach((button) -> button.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void keyTyped(char character, int code) {
        super.keyTyped(character, code);
        this.panels.forEach((button) -> button.keyTyped(character, code));
    }

    public static ClickGUIComponent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUIComponent();
        }

        return INSTANCE;
    }
}
