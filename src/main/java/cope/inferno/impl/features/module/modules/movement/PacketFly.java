package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.PushEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.MovementUtil;
import cope.inferno.util.timing.Timer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Module.Define(name = "PacketFly", category = Module.Category.Movement)
@Module.Info(description = "Flies with packets")
public class PacketFly extends Module {
    public final Setting<Bounds> bounds = new Setting<>("Bounds", Bounds.Negative);
    public final Setting<Phase> phase = new Setting<>("Phase", Phase.Semi);
    public final Setting<Double> speed = new Setting<>("Speed", 1.5, 0.1, 10.0);
    public final Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    private final Map<Integer, Timer> teleports = new ConcurrentHashMap<>();
    private int teleportId = 0;

    @Override
    protected void onDeactivated() {
        this.teleports.clear();
        this.teleportId = 0;
    }

    @Override
    public void onTick() {
        this.teleports.forEach((tpId, timer) -> {
            if (timer.passedS(10.0)) {
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
                this.teleports.remove(tpId);
            }
        });
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);

        if (this.phase.getValue() != Phase.None) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE) {
            mc.player.setVelocity(0.0, 0.0, 0.0);

            double speed = this.speed.getValue() / 10.0;
            if (this.isInBlock()) {
                speed /= 2.5; // slow down when we're in a block
            }

            double[] motion = MovementUtil.getDirectionalSpeed(speed);

            double motionY = mc.player.motionY;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                motionY = -(this.speed.getValue() / 10.0);
            } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = this.speed.getValue() / 10.0;
            } else {
                if (this.antiKick.getValue() && !MovementUtil.isMoving()) {
                    motionY = -(mc.player.ticksExisted % 2 == 0 ? 0.012253 : 0.0245);
                }
            }

            mc.player.setVelocity(motion[0], motionY, motion[1]);
            this.sendMovementPackets(mc.player.getPositionVector().add(mc.player.motionX, mc.player.motionY, mc.player.motionZ));
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            int id = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();

            this.teleports.put(id, new Timer().reset());
            this.teleportId = id;
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        event.setCanceled(event.getMaterial() == PushEvent.Type.BLOCKS && event.getEntity() == mc.player && this.phase.getValue() != Phase.None);
    }

    private void sendMovementPackets(Vec3d pos) {
        this.sendPacket(pos);
        if (this.bounds.getValue() != Bounds.None) {
            this.sendPacket(this.getOutOfBoundsVec(pos));
        }
    }

    private void sendPacket(Vec3d vec) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(vec.x, vec.y, vec.z, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId++));
        this.teleports.put(this.teleportId, new Timer().reset());
    }

    private Vec3d getOutOfBoundsVec(Vec3d pos) {
        Vec3d vec = pos.add(0.0, this.bounds.getValue().offset, 0.0);
        if (this.bounds.getValue() == Bounds.Random) {
            vec.add(0.0, ThreadLocalRandom.current().nextDouble(-1337.0, 1337.0), 0.0);
        }

        return vec;
    }

    private boolean isInBlock() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
    }

    public enum Bounds {
        Positive(1337.0),
        Negative(-1337.0),
        Random(0.0),
        None(0.0);

        private final double offset;
        Bounds(double offset) {
            this.offset = offset;
        }
    }

    public enum Phase {
        None, Semi, Full
    }
}
