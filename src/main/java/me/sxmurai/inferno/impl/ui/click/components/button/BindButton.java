package me.sxmurai.inferno.impl.ui.click.components.button;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.render.ScaleUtil;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.ui.components.widgets.button.Button;
import org.lwjgl.input.Keyboard;

public class BindButton extends Button {
    private final Module module;
    private boolean waiting = false;

    public BindButton(Module module) {
        super("Bind");
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        String text = this.waiting ? "Listening..." : ("Bind: " + (this.module.getBind() == -1 ? "None" : Keyboard.getKeyName(this.module.getBind())));
        Inferno.fontManager.drawCorrectString(text, (float) (this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.waiting = !this.waiting;
        }
    }

    @Override
    public void keyTyped(char character, int code) {
        if (this.waiting) {
            this.waiting = false;

            if (code == Keyboard.KEY_ESCAPE) {
                this.module.setBind(-1);
                return;
            }

            this.module.setBind(code);
        }
    }
}
