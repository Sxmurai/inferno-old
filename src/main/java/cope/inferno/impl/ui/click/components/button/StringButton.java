package cope.inferno.impl.ui.click.components.button;

import cope.inferno.Inferno;
import cope.inferno.impl.settings.Setting;
import cope.inferno.impl.ui.Animation;
import cope.inferno.impl.ui.components.widgets.button.Button;
import cope.inferno.util.render.ScaleUtil;
import org.lwjgl.input.Keyboard;

// i hate everything about the code here
public class StringButton extends Button {
    private final Setting<String> setting;

    private final Animation animation = new Animation(0.0f, 0.65f, 75L, true);
    private boolean typing = false;

    public StringButton(Setting<String> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        if (!this.typing && !this.isMouseInBounds(mouseX, mouseY)) {
            if (this.setting.getValue().isEmpty()) {
                this.setting.setValue(this.setting.getDefaultValue());
            }

            this.animation.setProgress(0.0f);
            Inferno.fontManager.drawCorrectString(this.name, ((float) this.x) + 2.3f, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
        } else {
            String content = this.setting.getValue();
            int width = Inferno.fontManager.getWidth(content);

            // goddamn this is some horrible code
            float offset = 0.0f;
            if (!this.typing) {
                this.animation.setMax(width);
                if (width > this.width) {
                    this.animation.update(this.animation.getProgress() <= this.animation.getMax());
                    offset = this.animation.getProgress();
                }
            } else {
                if (width > this.width) {
                    offset = (float) (this.width - width);
                }
            }

            Inferno.fontManager.drawCorrectString(content, (((float) this.x) + 2.3f) + offset, ScaleUtil.centerTextY((float) this.y, (float) this.height), -1);
        }
    }

    @Override
    public void doAction(int button) {
        if (button == 0) {
            this.typing = !this.typing;
        }
    }

    @Override
    public void keyTyped(char character, int code) {
        if (this.typing) {
            if (code == Keyboard.KEY_RETURN) {
                this.typing = false;
            } else {
                // for the love of fucking god
                if (code == Keyboard.KEY_UP || code == Keyboard.KEY_DOWN || code == Keyboard.KEY_LEFT || code == Keyboard.KEY_RIGHT || code == Keyboard.KEY_LSHIFT || code == Keyboard.KEY_RSHIFT) {
                    return;
                }

                String actual = "";
                if (code == Keyboard.KEY_SPACE) {
                    actual = " ";
                } else {
                    String val = Keyboard.getKeyName(code);
                    if (val == null) {
                        return;
                    }

                    actual = String.valueOf(character).toLowerCase();
                    if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                        actual = actual.toUpperCase();
                    }
                }

                if (code == Keyboard.KEY_BACK) {
                    if (this.setting.getValue().length() == 0) {
                        return;
                    }

                    this.setting.setValue(this.setting.getValue().substring(0, this.setting.getValue().length() - 1));
                } else {
                    this.setting.setValue(this.setting.getValue() + actual);
                }
            }
        }
    }
}
