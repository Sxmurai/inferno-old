package cope.inferno.core.gui.click;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.features.module.client.ClickGUI;
import cope.inferno.core.gui.click.components.Frame;
import cope.inferno.core.gui.click.components.buttons.ModuleButton;
import cope.inferno.core.shader.two.BlurShader;
import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.text.FormatUtil;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGUIScreen extends GuiScreen implements Wrapper {
    private static ClickGUIScreen INSTANCE;

    private final ArrayList<Frame> frames = new ArrayList<>();
    private final BlurShader blurShader = new BlurShader();

    private ClickGUIScreen() {
        double x = 6.0;

        for (Category category : Category.values()) {
            List<Module> modules = getInferno().getModuleManager().getModules()
                    .stream().filter((mod) -> mod.getCategory().equals(category))
                    .collect(Collectors.toList());

            if (!modules.isEmpty()) {
                frames.add(new Frame(FormatUtil.formatName(category.name()), x, 22.0) {
                    @Override
                    public void init() {
                        modules.forEach((module) -> children.add(new ModuleButton(module)));
                    }
                });

                x += 111.0;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        switch (ClickGUI.background.getValue()) {
            case BLUR: {
                blurShader.setIntensity(ClickGUI.intensity.getValue().floatValue());
                blurShader.render(partialTicks);
                break;
            }

            case DEFAULT: {
                drawDefaultBackground();
                break;
            }
        }

        frames.forEach((frame) -> frame.render(mouseX, mouseY));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        frames.forEach((frame) -> frame.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        frames.forEach((frame) -> frame.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        frames.forEach((frame) -> frame.keyTyped(typedChar, keyCode));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pause.getValue();
    }

    public static ClickGUIScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUIScreen();
        }

        return INSTANCE;
    }
}
