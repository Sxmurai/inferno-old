package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.PushEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.event.world.AddBoxToListEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.MovementUtil;
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
    public String getDisplayInfo() {
        return String.valueOf(this.factor.getValue());
    }

    @Override
    protected void onDeactivated() {
        this.teleportId = 0;

        if (fullNullCheck()) {
            Wrapper.mc.player.noClip = false;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        event.setX(Wrapper.mc.player.motionX);
        event.setY(Wrapper.mc.player.motionY);
        event.setZ(Wrapper.mc.player.motionZ);
        Wrapper.mc.player.setVelocity(event.getX(), event.getY(), event.getZ());
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE) {
            Wrapper.mc.player.setVelocity(0.0, 0.0, 0.0);
            Wrapper.mc.player.noClip = true;

            if (MovementUtil.isMoving() || Wrapper.mc.gameSettings.keyBindSneak.isKeyDown() || Wrapper.mc.gameSettings.keyBindJump.isKeyDown()) {
                if (MovementUtil.isMoving()) {
                    double speed = this.factor.getValue() / 10.0;
                    if (this.isClipped()) {
                        speed /= 2.5; // slow down while clipped in a block
                    }

                    double[] motion = MovementUtil.getDirectionalSpeed(speed);
                    Wrapper.mc.player.motionX = motion[0];
                    Wrapper.mc.player.motionZ = motion[1];
                }

                Wrapper.mc.player.setVelocity(Wrapper.mc.player.motionX, this.getMotionY(), Wrapper.mc.player.motionZ);
                this.send(Wrapper.mc.player.motionX, Wrapper.mc.player.motionY, Wrapper.mc.player.motionZ);
            }
        }
    }

    @SubscribeEvent
    public void onAddBoxToList(AddBoxToListEvent event) {
        event.setCanceled(event.getEntity() == Wrapper.mc.player && this.phase.getValue() != Phase.None);
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        event.setCanceled(event.getEntity() == Wrapper.mc.player && event.getMaterial() == PushEvent.Type.BLOCKS);
    }

    private void send(double x, double y, double z) {
        Vec3d pos = Wrapper.mc.player.getPositionVector().add(x, y, z);

        this.sendPacket(pos);
        this.sendPacket(this.getOutOfBoundsVec(pos)); // send a completely invalid packet to fuck with NCP's setback

        if (this.limitJitter.getValue()) {
            Wrapper.mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId));
        }
    }

    private void sendPacket(Vec3d pos) {
        Wrapper.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(pos.x, pos.y, pos.z, Inferno.rotationManager.getYaw(true), Inferno.rotationManager.getPitch(true), Wrapper.mc.player.onGround));
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
        double motionY = Wrapper.mc.player.motionY;
        if (Wrapper.mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -(this.factor.getValue() / 10.0);
        } else if (Wrapper.mc.gameSettings.keyBindJump.isKeyDown()) {
            motionY = this.factor.getValue() / 10.0;
        }

        if (this.antiKick.getValue() && !this.isClipped() && !Wrapper.mc.gameSettings.keyBindJump.isKeyDown() && !Wrapper.mc.gameSettings.keyBindSneak.isKeyDown()) { // we dont want to go down while clipped, but if we're in the air we'll fall every once in awhile
            if (Wrapper.mc.player.ticksExisted % 8 == 2) {
                motionY = -0.0356;
            } else if (Wrapper.mc.player.ticksExisted % 8 == 4) {
                motionY = -0.102;
            } else if (Wrapper.mc.player.ticksExisted % 8 == 6) {
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
        return !Wrapper.mc.world.getCollisionBoxes(Wrapper.mc.player, Wrapper.mc.player.getEntityBoundingBox().contract(0.0625, 0.0625, 0.0625)).isEmpty();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            if (packet.flags.isEmpty()) {
                return;
            }

            if (this.limitJitter.getValue()) {
                if (this.teleportId == 0) {
                    this.teleportId = packet.getTeleportId() + 1;
                } else {
                    ++this.teleportId;
                }

                Wrapper.mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
            }

            if (this.sync.getValue()) {
                Wrapper.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), Wrapper.mc.player.onGround));
                Wrapper.mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
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
