package cope.inferno.core.features.module.movement;

import cope.inferno.core.events.MoveEvent;
import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.events.UpdateWalkingPlayerEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.player.MotionUtil;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PacketFly extends Module {
    public PacketFly() {
        super("PacketFly", Category.MOVEMENT, "NCP exploit to fly with packets");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.SETBACK);

    public static final Setting<Phase> phase = new Setting<>("Phase", Phase.NONE);

    public static final Setting<Direction> direction = new Setting<>("Direction", Direction.NEGATIVE);

    public static final Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.1f, 10.0f);
    public static final Setting<Integer> factor = new Setting<>("Factor", 2, 1, 10);

    public static final Setting<Boolean> answer = new Setting<>("Answer", true);
    public static final Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    private final Map<Integer, Vec3d> teleports = new HashMap<>();
    private int teleportId = 0;

    private final Timer timer = new Timer();

    private int packets = 0;

    @Override
    protected void onDisable() {
        teleports.clear();
        packets = 0;

        NetworkUtil.send(new CPacketConfirmTeleport(teleportId++));
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);

        if (!phase.getValue().equals(Phase.NONE)) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        mc.player.setVelocity(0.0, 0.0, 0.0);
        event.setCanceled(true);

        double smallerSpeed = speed.getValue() / 10.0;

        double multiplier = packets % 2 == 0 ? 0.87 : 0.7442;
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY += smallerSpeed * multiplier;

            if (mc.player.ticksExisted % 6 == 0) {
                mc.player.motionY = -0.08;
            }
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY -= smallerSpeed * multiplier;
        }

        if (antiKick.getValue() && timer.passed(4L, Format.TICKS)) {
            timer.reset();
            mc.player.motionY = -0.04;
        }

        if (MotionUtil.isMoving()) {
            double[] motion = MotionUtil.strafe(smallerSpeed);

            if (mode.getValue().equals(Mode.SETBACK)) {
                mc.player.motionX = motion[0];
                mc.player.motionZ = motion[1];
            } else if (mode.getValue().equals(Mode.FACTOR)) {
                for (int i = 0; i < factor.getValue(); ++i) {
                    mc.player.motionX = motion[0] * i * smallerSpeed;
                    mc.player.motionZ = motion[1] * i * smallerSpeed;

                    sendPackets(getPlayerPos().add(mc.player.motionX, mc.player.motionY, mc.player.motionZ), false);
                }
            }
        }

        sendPackets(getPlayerPos().add(mc.player.motionX, mc.player.motionY, mc.player.motionZ), true);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            ++packets;
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
            SPacketPlayerPosLook packet = event.getPacket();

            double x = packet.getX();
            double y = packet.getY();
            double z = packet.getZ();

            int tpId = packet.teleportId;

            Vec3d vec = teleports.remove(tpId);
            if (vec != null && (vec.x == x && vec.y == y && vec.z == z)) {
                event.setCanceled(true);
                return;
            }

            teleportId = tpId;

            if (answer.getValue()) {
                event.setCanceled(true);

                Set<SPacketPlayerPosLook.EnumFlags> flags = packet.getFlags();

                float yaw = packet.getYaw();
                float pitch = packet.getPitch();

                if (flags.contains(SPacketPlayerPosLook.EnumFlags.X)) {
                    x += mc.player.posX;
                } else {
                    mc.player.motionX = 0.0;
                }

                if (flags.contains(SPacketPlayerPosLook.EnumFlags.Y)) {
                    y += mc.player.posY;
                } else {
                    mc.player.motionY = 0.0;
                }

                if (flags.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
                    z += mc.player.posZ;
                } else {
                    mc.player.motionZ = 0.0;
                }

                if (flags.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
                    pitch += mc.player.rotationPitch;
                }

                if (flags.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
                    yaw += mc.player.rotationYaw;
                }

                mc.player.setPosition(x, y, z);

                NetworkUtil.sendNoEvent(new CPacketConfirmTeleport(packet.teleportId));
                NetworkUtil.sendNoEvent(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, yaw, pitch, false));
            }

            packet.yaw = mc.player.rotationYaw;
            packet.pitch = mc.player.rotationPitch;
        }
    }

    private Vec3d getPlayerPos() {
        return new Vec3d(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ);
    }

    private void sendPackets(Vec3d pos, boolean invalid) {
        sendPacket(pos.x, pos.y, pos.z);

        if (invalid) {
            Vec3d outOfBounds = getOutOfBoundsVec(pos);
            sendPacket(outOfBounds.x, outOfBounds.y, outOfBounds.z);
        }

        NetworkUtil.send(new CPacketConfirmTeleport(teleportId++));
        teleports.put(teleportId, pos);
    }

    private void sendPacket(double x, double y, double z) {
        NetworkUtil.send(new CPacketPlayer.PositionRotation(x, y, z, mc.player.rotationYaw, mc.player.rotationPitch, false));
    }

    private Vec3d getOutOfBoundsVec(Vec3d pos) {
        if (direction.getValue().equals(Direction.NONE)) {
            return pos;
        }

        return pos.add(
                0.0,
                direction.getValue().equals(Direction.RANDOM) ?
                        ThreadLocalRandom.current().nextDouble(-69420.0, 69420.0) :
                        direction.getValue().offset,
                0.0
        );
    }

    public enum Mode {
        /**
         * Default packet fly, sends movement packets along with invalid packets
         */
        SETBACK,

        /**
         * Uses a loop and multiplies the index as the factor
         */
        FACTOR
    }

    public enum Phase {
        /**
         * Do not try to phase, only fly
         */
        NONE,

        /**
         * Vanilla phase
         */
        SEMI,

        /**
         * NCP Phase
         */
        FULL
    }

    public enum Direction {
        NONE(0.0),
        RANDOM(0.0),
        NEGATIVE(-69420.0),
        POSITIVE(69420.0);

        private final double offset;

        Direction(double offset) {
            this.offset = offset;
        }
    }
}
