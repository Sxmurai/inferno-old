package me.sxmurai.inferno.impl.features.module.modules.miscellaneous;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "NoRotate")
@Module.Info(description = "Stops you from processing server rotation packets. Might cause de-syncs.")
public class NoRotate extends Module {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            packet.yaw = Inferno.rotationManager.getYaw(true);
            packet.pitch = Inferno.rotationManager.getPitch(true);
        }
    }
}
