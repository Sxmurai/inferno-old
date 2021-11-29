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
    }

    @Override
    public void init() {
        double x = 4.0;
        for (Module.Category category : Module.Category.values()) {
            List<Module> modules = Inferno.moduleManager.getModules()
                    .stream().filter((module) -> module.getCategory().equals(category))
                    .collect(Collectors.toList());

            if (modules.isEmpty()) {
                continue;
            }

            this.children.add(new ModuleFrame(category.name(), x, 26.0) {
                @Override
                public void init() {
                    modules.forEach((module) -> this.children.add(new ModuleButton(module)));
                }
            });
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY) {

    }

    public static ClickGuiComponent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiComponent();
        }

        return INSTANCE;
    }
}
