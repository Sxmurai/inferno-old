package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Portal", category = Module.Category.Player)
@Module.Info(description = "Modifies portal behavior")
public class Portal extends Module {
    public final Setting<Boolean> godmode = new Setting<>("Godmode", false);
    public final Setting<Boolean> gui = new Setting<>("GUI", true);

    @Override
    public void onUpdate() {
        mc.player.inPortal = mc.player.inPortal && !this.gui.getValue();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        // i think this is the right packet?
        if (event.getPacket() instanceof CPacketConfirmTeleport && mc.player.timeInPortal > 0.0f && this.godmode.getValue()) {
            event.setCanceled(true);
        }
    }
}
