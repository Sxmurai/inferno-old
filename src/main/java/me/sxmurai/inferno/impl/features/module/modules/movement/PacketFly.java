package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.MoveEvent;
import me.sxmurai.inferno.impl.event.entity.PushEvent;
import me.sxmurai.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.event.world.AddBoxToListEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.MovementUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

@Module.Define(name = "PacketFly", category = Module.Category.Movement)
@Module.Info(description = "Flies with packets")
public class PacketFly extends Module {
    public final Setting<Bounds> bounds = new Setting<>("Bounds", Bounds.Positive);
    public final Setting<Double> factor = new Setting<>("Factor", 1.5, 0.1, 5.0);
    public final Setting<Phase> phase = new Setting<>("Phase", Phase.Semi);
    public final Setting<Boolean> antiKick = new Setting<>("AntiKick", true);
    public final Setting<Boolean> limitJitter = new Setting<>("LimitJitter", true);
    public final Setting<Boolean> sync = new Setting<>("Sync", true);

    private int teleportId = 0;

    @Override
    protected void onDeactivated() {
        this.teleportId = 0;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);
        mc.player.setVelocity(event.getX(), event.getY(), event.getZ());
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE) {
            mc.player.setVelocity(0.0, 0.0, 0.0);
            mc.player.noClip = true;

            if (MovementUtil.isMoving() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindJump.isKeyDown()) {
                double speed = this.factor.getValue() / 10.0;
                if (this.isClipped()) {
                    speed /= 2.5; // slow down while clipped in a block
                }

                double[] motion = MovementUtil.getDirectionalSpeed(speed);

                mc.player.setVelocity(motion[0], this.getMotionY(), motion[1]);
                this.send(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
            }
        }
    }

    @SubscribeEvent
    public void onAddBoxToList(AddBoxToListEvent event) {
        event.setCanceled(event.getEntity() == mc.player && this.phase.getValue() != Phase.None);
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getMaterial() == PushEvent.Type.BLOCKS && event.getEntity() == mc.player) {
            event.setCanceled(this.phase.getValue() != Phase.None);
        }
    }

    private void send(double x, double y, double z) {
        Vec3d pos = mc.player.getPositionVector().add(x, y, z);

        this.sendPacket(pos);
        this.sendPacket(this.getOutOfBoundsVec(pos)); // send a completely invalid packet to fuck with NCP's setback

        if (this.limitJitter.getValue()) {
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId));
            ++this.teleportId;
        }
    }

    private void sendPacket(Vec3d pos) {
        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(pos.x, pos.y, pos.z, Inferno.rotationManager.getYaw(true), Inferno.rotationManager.getPitch(true), mc.player.onGround));
    }

    private Vec3d getOutOfBoundsVec(Vec3d pos) {
        Bounds bounds = this.bounds.getValue();
        if (bounds == Bounds.None) {
            return pos;
        } else if (bounds == Bounds.Random) {
            return pos.add(0.0, this.random(-1337.0, 1337.0), 0.0);
        }

        return pos.add(bounds.x, bounds.y, bounds.z);
    }

    private double getMotionY() {
        double motionY = mc.player.motionY;
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -(this.factor.getValue() / 10.0);
        } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
            motionY = this.factor.getValue() / 10.0;
        }

        if (this.antiKick.getValue() && !this.isClipped() && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) { // we dont want to go down while clipped, but if we're in the air we'll fall every once in awhile
            if (mc.player.ticksExisted % 8 == 2) {
                motionY = -0.0356;
            } else if (mc.player.ticksExisted % 8 == 4) {
                motionY = -0.102;
            } else if (mc.player.ticksExisted % 8 == 6) {
                motionY = -0.1892;
            }
        }

        return motionY;
    }

    private double random(double start, double end) {
        if (end > start) {
            return end;
        }

        return ThreadLocalRandom.current().nextDouble(start, end);
    }

    private boolean isClipped() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(0.0625, 0.0625, 0.0625)).isEmpty();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            if (packet.flags.isEmpty()) {
                return;
            }

            if (this.limitJitter.getValue()) {
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                ++this.teleportId;
            }

            if (this.sync.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), mc.player.onGround));
                mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
            }
        }
    }

    public enum Bounds {
        None(0.0, 0.0, 0.0),
        Positive(0.0, 1337.0, 0.0),
        Negative(0.0, -1337.0, 0.0),
        Random(0.0, -1.0, 0.0);

        private final double x, y, z;

        Bounds(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public enum Phase {
        None, Semi, Full
    }
}
