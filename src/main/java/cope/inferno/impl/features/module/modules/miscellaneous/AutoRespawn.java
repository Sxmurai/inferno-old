package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.timing.Timer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "AutoRespawn")
@Module.Info(description = "Automatically respawns you")
public class AutoRespawn extends Module {
    public final Setting<Float> delay = new Setting<>("Delay", 0.0f, 0.0f, 5.0f);

    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if (this.delay.getValue() == 0.0f) {
                Wrapper.mc.player.respawnPlayer();
                return;
            }

            this.timer.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (this.timer.passedS(this.delay.getValue().doubleValue()) && (EntityUtil.getHealth(Wrapper.mc.player) <= 0.0f || Wrapper.mc.player.isDead || Wrapper.mc.currentScreen instanceof GuiGameOver)) { // we have to make sure they havent already respawned
            Wrapper.mc.player.respawnPlayer();
        }
    }
}
