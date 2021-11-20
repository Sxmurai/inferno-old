package me.sxmurai.inferno.impl.features.module.modules.client;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.impl.ui.InfernoGUI;
import org.lwjgl.input.Keyboard;

@Module.Define(name = "GUI", category = Module.Category.Client)
@Module.Info(description = "Displays the clients GUI", bind = Keyboard.KEY_R)
public class GUI extends Module {
    public static GUI INSTANCE;

    public static Setting<Boolean> pause = new Setting<>("Pause", false);
    public static Setting<Boolean> tooltips = new Setting<>("Tooltips", true);

    public GUI() {
        INSTANCE = this;
    }

    @Override
    protected void onActivated() {
        if (!fullNullCheck()) {
            this.toggle();
            return;
        }

        mc.displayGuiScreen(InfernoGUI.getInstance());
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            mc.displayGuiScreen(null);
        }
    }
}
