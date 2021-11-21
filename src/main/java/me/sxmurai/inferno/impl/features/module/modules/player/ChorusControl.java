package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "ChorusControl", category = Module.Category.Player)
@Module.Info(description = "Cancels chorus teleports")
public class ChorusControl extends Module {
    public final Setting<Boolean> cancel = new Setting<>("Cancel", false);

    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            while (!this.packets.isEmpty()) {
                Packet<?> packet = this.packets.poll();
                if (packet == null) {
                    break;
                }

                mc.player.connection.sendPacket(packet);
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && this.cancel.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketConfirmTeleport) {
            this.packets.add(event.getPacket());
        }
    }
}
