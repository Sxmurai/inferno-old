package cope.inferno.util.entity.player;

import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    public static Vec3d getCentered() {
        return new Vec3d(Math.floor(mc.player.posX) + 0.5, mc.player.posY, Math.floor(mc.player.posZ) + 0.5);
    }

    /**
     * Gets the local player's BlockPos position
     * @return a BlockPos object
     */
    public static BlockPos getLocalPosition() {
        return new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    }
}
