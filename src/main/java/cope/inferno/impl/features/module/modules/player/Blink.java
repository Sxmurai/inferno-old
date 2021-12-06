package cope.inferno.impl.features.module.modules.player;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.event.network.SelfConnectionEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.timing.Timer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "Blink", category = Module.Category.Player)
@Module.Info(description = "Suspends movement packets until a threshold is reached")
public class Blink extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Manual);
    public final Setting<Boolean> fakePlayer = new Setting<>("FakePlayer", true);

    public final Setting<Double> distance = new Setting<>("Distance", 10.0, 1.0, 100.0, () -> this.mode.getValue() == Mode.Distance);
    public final Setting<Float> time = new Setting<>("Time", 10.0f, 1.0f, 20.0f, () -> this.mode.getValue() == Mode.Time);
    public final Setting<Integer> packets = new Setting<>("Packets", 20, 1, 100, () -> this.mode.getValue() == Mode.Packets);

    private final Queue<CPacketPlayer> queue = new ConcurrentLinkedQueue<>();
    private boolean sending = false;

    private EntityOtherPlayerMP fake = null;
    private BlockPos pos;
    private final Timer timer = new Timer();

    @Override
    protected void onActivated() {
        if (!fullNullCheck()) {
            this.toggle();
            return;
        }

        if (this.fakePlayer.getValue()) {
            this.spawn(false);
        }
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            this.empty();
            this.spawn(true);
        }

        this.fake = null;
        this.pos = null;
    }

    @Override
    public void onUpdate() {
        switch (this.mode.getValue()) {
            case Distance: {
                if (this.pos == null) {
                    this.pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                } else {
                    if (mc.player.getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) >= this.distance.getValue()) {
                        this.pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                        this.reset();
                    }
                }
                break;
            }

            case Time: {
                if (this.timer.passedS(this.time.getValue())) {
                    this.timer.reset();
                    this.reset();
                }
                break;
            }

            case Packets: {
                if (this.queue.size() >= this.packets.getValue()) {
                    this.reset();
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onSelfConnection(SelfConnectionEvent event) {
        if (event.getType() == SelfConnectionEvent.Type.Disconnect) {
            this.toggle();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && !this.sending) {
            this.queue.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    private void reset() {
        this.empty();
        this.spawn(true);
        this.spawn(false);
    }

    private void empty() {
        if (this.sending) {
            return;
        }

        this.sending = true;
        while (!this.queue.isEmpty()) {
            CPacketPlayer packet = this.queue.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }
        this.sending = false;
    }

    private void spawn(boolean despawn) {
        if (despawn) {
            if (this.fake != null) {
                mc.world.removeEntity(this.fake);
                mc.world.removeEntityDangerously(this.fake);
            }
        } else {
            this.fake = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            this.fake.copyLocationAndAnglesFrom(mc.player);
            this.fake.inventory.copyInventory(mc.player.inventory);

            mc.world.spawnEntity(this.fake);
        }
    }

    public enum Mode {
        Manual, Distance, Time, Packets
    }
}
