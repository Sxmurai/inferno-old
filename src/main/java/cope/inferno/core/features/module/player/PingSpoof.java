package cope.inferno.core.features.module.player;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.internal.timing.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingSpoof extends Module {
    public PingSpoof() {
        super("PingSpoof", Category.PLAYER, "Spoofs your ping to the server");
    }

    public static final Setting<Float> delay = new Setting<>("Delay", 0.5f, 0.1f, 10.0f);
    public static final Setting<Boolean> bypass = new Setting<>("Bypass", false);

    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private boolean sending = false;

    @Override
    protected void onDisable() {
        if (nullCheck()) {
            emptyQueue();
        }
    }

    @Override
    public void onUpdate() {
        if (timer.passedMs((long) (delay.getValue() * 1000.0f))) {
            timer.reset();
            emptyQueue();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if ((event.getPacket() instanceof CPacketKeepAlive || bypass.getValue() && event.getPacket() instanceof CPacketConfirmTransaction) && !sending) {
            packets.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    private void emptyQueue() {
        sending = true;

        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        sending = false;
    }
}
