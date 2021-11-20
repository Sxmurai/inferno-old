package me.sxmurai.inferno.util.entity;

import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MovementUtil implements Wrapper {
    public static boolean isMoving() {
        return mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f;
    }

    public static double[] getDirectionalSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward,
                strafe = mc.player.movementInput.moveStrafe,
                yaw = mc.player.rotationYaw;

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

        double x = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));
        double z = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));

        return new double[] { x, z };
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
