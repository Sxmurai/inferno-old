package cope.inferno.impl.manager;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.util.entity.RotationUtil;
import cope.inferno.util.timing.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RotationManager implements Wrapper {
    private final RotationUtil.Rotation rotation = new RotationUtil.Rotation();
    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            if (packet.rotating && this.rotation.isValid()) {
                packet.yaw = this.rotation.getYaw();
                packet.pitch = this.rotation.getPitch();
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE) {
            if (this.timer.passedMs(375L)) {
                this.reset();
            } else {
                event.setYaw(this.getYaw(true));
                event.setPitch(this.getPitch(true));
                event.setCanceled(true);
            }
        }
    }

    private void reset() {
        this.rotation.set(-1.0f, -1.0f);
    }

    public void setRotations(float yaw, float pitch) {
        this.timer.reset();
        this.rotation.set(yaw, pitch);
    }

    public void look(Entity entity) {
        this.look(entity.getPositionEyes(mc.getRenderPartialTicks()));
    }

    public void look(BlockPos pos) {
        this.look(new Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5));
    }

    public void look(Vec3d vec) {
        RotationUtil.Rotation rotations = RotationUtil.calcRotations(vec);
        this.setRotations(rotations.getYaw(), rotations.getPitch());
    }

    public float getYaw() {
        return this.getYaw(false);
    }

    public float getYaw(boolean safe) {
        float yaw = this.rotation.getYaw();
        return yaw == -1.0f && safe ? mc.player.rotationYaw : yaw;
    }

    public float getPitch() {
        return this.getPitch(false);
    }

    public float getPitch(boolean safe) {
        float pitch = this.rotation.getPitch();
        return pitch == -1.0f && safe ? mc.player.rotationPitch : pitch;
    }

    public boolean isValid() {
        return this.rotation.isValid();
    }
}
