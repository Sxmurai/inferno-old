package cope.inferno.util.entity;

import cope.inferno.impl.features.Wrapper;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

public class MovementUtil implements Wrapper {
    public static boolean isMoving() {
        return mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f;
    }

    public static double[] getDirectionalSpeed(double speed) {
        float[] movements = getMovement();

        float forward = movements[0];
        float strafe = movements[1];

        double sin = -Math.sin(Math.toRadians(movements[2]));
        double cos = Math.cos(Math.toRadians(movements[2]));

        return new double[] {
                forward * speed * sin + strafe * speed * cos,
                forward * speed * cos - strafe * speed * sin
        };
    }

    public static float[] getMovement() {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;

        float yaw = mc.player.rotationYaw;

        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (strafe < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }

            strafe = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        return new float[] { forward, strafe, yaw, };
    }

    public static void center(Center center, double tolerance) {
        if (center == Center.None) {
            return;
        }

        Vec3d centered = MovementUtil.getCentered();
        if (Math.abs(mc.player.posX - centered.x) > tolerance || Math.abs(mc.player.posZ - centered.z) > tolerance) {
            if (center == Center.Teleport) {
                MovementUtil.setPosition(centered.x, centered.y, centered.z);
            } else {
                // chinese
                mc.player.motionX = (centered.x - mc.player.posX) / 2.0;
                mc.player.motionZ = (centered.z - mc.player.posZ) / 2.0;

                MovementUtil.setPosition(centered.x + mc.player.motionX, centered.y, centered.z + mc.player.motionZ);
            }
        }
    }

    public static Vec3d getCentered() {
        return new Vec3d(Math.floor(mc.player.posX) + 0.5, mc.player.posY, Math.floor(mc.player.posZ) + 0.5);
    }

    public static void setPosition(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, mc.player.onGround));
        mc.player.setPosition(x, y, z);
    }

    public enum Center {
        None, Motion, Teleport
    }
}
