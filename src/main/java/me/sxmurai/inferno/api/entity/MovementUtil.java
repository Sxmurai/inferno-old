package me.sxmurai.inferno.api.entity;

import me.sxmurai.inferno.impl.features.Wrapper;

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
}
