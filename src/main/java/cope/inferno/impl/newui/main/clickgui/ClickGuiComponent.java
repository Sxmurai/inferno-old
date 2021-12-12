package cope.inferno.impl.newui.main.clickgui;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.newui.components.AbstractComponent;
import cope.inferno.impl.newui.main.clickgui.interactables.ModuleButton;

import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiComponent extends AbstractComponent {
    private static ClickGuiComponent INSTANCE;

    public ClickGuiComponent() {
        super("clickgui", -1.0, -1.0, -1.0, -1.0);
        this.init();
    }

    @Override
    public void init() {
        double x = 4.0;
        for (Module.Category category : Module.Category.values()) {
            List<Module> modules = Inferno.moduleManager.getModules().stream().filter((module) -> module.getCategory().equals(category)).collect(Collectors.toList());
            if (modules.isEmpty()) {
                continue;
            }

            this.children.add(new ModuleFrame(category.name(), x, 26.0) {
                @Override
                public void init() {
                    modules.forEach((module) -> this.children.add(new ModuleButton(module)));
                }
            });

            x += 96.0;
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        this.children.forEach((child) -> child.onRender(mouseX, mouseY));
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        this.children.forEach((child) -> child.onMouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        super.onMouseReleased(mouseX, mouseY, state);
        this.children.forEach((child) -> child.onMouseReleased(mouseX, mouseY, state));
    }

    public static ClickGuiComponent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiComponent();
        }

        return INSTANCE;
    }
}
