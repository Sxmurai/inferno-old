package cope.inferno.impl.features.module.modules.player;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.timing.Timer;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "PingSpoof", category = Module.Category.Player)
@Module.Info(description = "Makes your ping higher")
public class PingSpoof extends Module {
    public final Setting<Double> delay = new Setting<>("Delay", 1.5, 0.1, 10.0);

    private final Queue<CPacketKeepAlive> packets = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private boolean sending = false;

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            this.timer.reset();
            this.empty();
        }
    }

    @Override
    public void onUpdate() {
        if (this.timer.passedS(this.delay.getValue())) {
            this.timer.reset();
            this.empty();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketKeepAlive && !this.sending) {
            this.packets.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    private void empty() {
        if (this.sending) {
            return;
        }

        this.sending = true;
        while (!this.packets.isEmpty()) {
            CPacketKeepAlive packet = this.packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        this.sending = false;
    }
}
