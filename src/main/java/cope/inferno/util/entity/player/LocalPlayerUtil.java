package cope.inferno.util.entity.player;

import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.network.play.client.CPacketEntityAction;

public class LocalPlayerUtil implements Wrapper {
    /**
     * Sends a sprint packet
     * @param sprint If to send a START_SPRINTING of a STOP_SPRINTING packet
     */
    public static void sprint(boolean sprint) {
        NetworkUtil.send(new CPacketEntityAction(mc.player, sprint ? CPacketEntityAction.Action.START_SPRINTING : CPacketEntityAction.Action.STOP_SPRINTING));
    }

    /**
     * Sends a sneak packet
     * @param sneak If to send a START_SNEAKING or a STOP_SNEAKING packet
     */
    public static void sneak(boolean sneak) {
        NetworkUtil.send(new CPacketEntityAction(mc.player, sneak ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
    }
}
