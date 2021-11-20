package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.timing.Timer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "AutoRespawn", category = Module.Category.Player)
@Module.Info(description = "Automatically respawns you")
public class AutoRespawn extends Module {
    public final Setting<Float> delay = new Setting<>("Delay", 0.0f, 0.0f, 5.0f);

    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if (this.delay.getValue() == 0.0f) {
                mc.player.respawnPlayer();
                return;
            }

            this.timer.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (this.timer.passedS(this.delay.getValue().doubleValue()) && (EntityUtil.getHealth(mc.player) <= 0.0f || mc.player.isDead || mc.currentScreen instanceof GuiGameOver)) { // we have to make sure they havent already respawned
            mc.player.respawnPlayer();
        }
    }
}
