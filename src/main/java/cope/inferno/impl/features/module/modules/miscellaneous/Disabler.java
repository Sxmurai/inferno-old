package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.timing.Timer;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Disabler")
@Module.Info(description = "Disables NCP rofl")
public class Disabler extends Module {
    public final Setting<Boolean> packet = new Setting<>("Packet", true);
    public final Setting<Double> delay = new Setting<>("Delay", 2.0, 0.1, 5.0);

    private final Timer timer = new Timer();

    @Override
    public void onUpdate() {
        if (this.timer.passedS(this.delay.getValue())) {
            this.timer.reset();
            mc.player.connection.sendPacket(new CPacketPlayerAbilities(mc.player.capabilities));
            mc.player.connection.getNetworkManager().sendPacket(new CPacketKeepAlive(), null);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketKeepAlive) {
            event.setCanceled(true);
        } else if (event.getPacket() instanceof CPacketConfirmTransaction) {
            event.setCanceled(true);
        } else if (event.getPacket() instanceof CPacketPlayerAbilities) {
            CPacketPlayerAbilities packet = event.getPacket();

            packet.setAllowFlying(true);
            packet.setCreativeMode(true);
            packet.setFlying(true);
            packet.setInvulnerable(true);
            packet.setFlySpeed(Float.POSITIVE_INFINITY);
            packet.setWalkSpeed(Float.POSITIVE_INFINITY);
        }
    }
}
