package cope.inferno.core.manager.managers;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.events.UpdateWalkingPlayerEvent;
import cope.inferno.util.entity.player.rotation.Rotation;
import cope.inferno.util.entity.player.rotation.RotationUtil;
import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.internal.timing.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RotationManager implements Wrapper {
    private final Rotation rotation = new Rotation();
    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();

            if (packet.rotating && rotation.isValid()) {
                packet.yaw = rotation.getYaw();
                packet.pitch = rotation.getPitch();
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra().equals(UpdateWalkingPlayerEvent.Era.PRE)) {
            if (timer.passedMs(500L)) {
                reset();
            } else {
                if (rotation.isValid()) {
                    event.setYaw(rotation.getYaw());
                    event.setPitch(rotation.getPitch());
                    event.setCanceled(true);
                }
            }
        }
    }

    public void rotate(Entity entity) {
        rotate(entity.getPositionEyes(mc.getRenderPartialTicks()));
    }

    public void rotate(BlockPos pos) {
        rotate(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public void rotate(Vec3d vec) {
        Rotation newRotation = RotationUtil.calcRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);

        setRotations(newRotation.getYaw(), newRotation.getPitch());
    }

    public void setRotations(float yaw, float pitch) {
        timer.reset();

        rotation.setYaw(yaw);
        rotation.setPitch(pitch);
    }

    public void reset() {
        rotation.reset();
    }

    public boolean isValid() {
        return rotation.isValid();
    }

    public float getYaw() {
        return rotation.getYaw();
    }

    public float getPitch() {
        return rotation.getPitch();
    }
}
