package me.sxmurai.inferno.impl.features.module.modules.render;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.impl.ui.Animation;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

@Module.Define(name = "Brightness", category = Module.Category.Render)
@Module.Info(description = "Makes the game brighter.")
public class Brightness extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Gamma);

    private final Animation animation = new Animation(100.0f, 0.3f, 22L, true);
    public float oldGamma = -1.0f;

    @Override
    protected void onDeactivated() {
        if (this.oldGamma != -1.0f) {
            mc.gameSettings.gammaSetting = this.oldGamma;
            this.oldGamma = -1.0f;
        }

        if (fullNullCheck() && mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    public void onUpdate() {
        if (this.mode.getValue() == Mode.Gamma) {
            if (this.oldGamma == -1.0f) {
                this.oldGamma = mc.gameSettings.gammaSetting;
            }

            if (mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
            }

            if (this.animation.getProgress() != 100.0f) {
                this.animation.update(false);
            }

            mc.gameSettings.gammaSetting = this.animation.getProgress();
        } else {
            if (this.animation.getProgress() > this.oldGamma) {
                this.animation.update(true);
                mc.gameSettings.gammaSetting = this.animation.getProgress();
                return;
            }

            this.oldGamma = -1.0f;
            if (!mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 99999));
            }
        }
    }

    public enum Mode {
        Gamma, Potion
    }
}
