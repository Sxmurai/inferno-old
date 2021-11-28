package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.impl.ui.Animation;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

@Module.Define(name = "Brightness", category = Module.Category.Render)
@Module.Info(description = "Makes the game brighter.")
public class Brightness extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Gamma);

    private final Animation animation = new Animation(100.0f, 0.3f, 22L, true);
    public float oldGamma = -1.0f;

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    @Override
    protected void onDeactivated() {
        if (this.oldGamma != -1.0f) {
            Wrapper.mc.gameSettings.gammaSetting = this.oldGamma;
            this.oldGamma = -1.0f;
        }

        if (fullNullCheck() && Wrapper.mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
            Wrapper.mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    public void onUpdate() {
        if (this.mode.getValue() == Mode.Gamma) {
            if (this.oldGamma == -1.0f) {
                this.oldGamma = Wrapper.mc.gameSettings.gammaSetting;
            }

            if (Wrapper.mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                Wrapper.mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
            }

            if (this.animation.getProgress() != 100.0f) {
                this.animation.update(false);
            }

            Wrapper.mc.gameSettings.gammaSetting = this.animation.getProgress();
        } else {
            if (this.animation.getProgress() > this.oldGamma) {
                this.animation.update(true);
                Wrapper.mc.gameSettings.gammaSetting = this.animation.getProgress();
                return;
            }

            this.oldGamma = -1.0f;
            if (!Wrapper.mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                Wrapper.mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 99999));
            }
        }
    }

    public enum Mode {
        Gamma, Potion
    }
}
