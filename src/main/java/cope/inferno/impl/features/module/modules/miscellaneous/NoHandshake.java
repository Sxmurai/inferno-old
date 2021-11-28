package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

@Module.Define(name = "NoHandshake")
@Module.Info(description = "Stops forge from being a cunt")
public class NoHandshake extends Module {
    public final Setting<Brand> brand = new Setting<>("Brand", Brand.Forge);

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck()) {
            if (event.getPacket() instanceof FMLProxyPacket && !Wrapper.mc.isSingleplayer()) {
                event.setCanceled(true);
            }

            if (event.getPacket() instanceof CPacketCustomPayload && this.brand.getValue() != Brand.Forge) {
                CPacketCustomPayload packet = (CPacketCustomPayload) event.getPacket();
                if (packet.getChannelName().equalsIgnoreCase("MC|Brand")) {
                    packet.data = new PacketBuffer(Unpooled.buffer()).writeString(this.brand.getValue().brand);
                }
            }
        }
    }

    public enum Brand {
        Forge(null), Vanilla("vanilla"), Lunar("Lunar-Client");

        private final String brand;
        Brand(String brand) {
            this.brand = brand;
        }
    }
}
