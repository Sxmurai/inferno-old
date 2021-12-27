package cope.inferno.util.network;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;

public class NetworkUtil implements Wrapper {
    /**
     * Receive a packet from the server, but not actually
     * @param packet The packet to receive via an event
     */
    public static void receive(Packet<?> packet) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.Receive(packet));
    }

    /**
     * Sends a packet to the server
     * @param packet The packet to send
     */
    public static void send(Packet<?> packet) {
        if (isOpen()) {
            mc.player.connection.sendPacket(packet);
        }
    }

    /**
     * Sends a packet to the server without an event being emitted
     * @param packet The packet to send
     */
    public static void sendNoEvent(Packet<?> packet) {
        if (isOpen()) {
            mc.player.connection.getNetworkManager().sendPacket(packet, null);
        }
    }

    /**
     * Checks if the network channel is open to send and receive packets
     * @return true if it is available, false if it is not
     */
    public static boolean isOpen() {
        return mc.player.connection.getNetworkManager().isChannelOpen();
    }
}
