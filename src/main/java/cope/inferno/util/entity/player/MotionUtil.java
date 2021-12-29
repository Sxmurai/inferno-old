package cope.inferno.util.entity.player;

import cope.inferno.util.internal.Wrapper;

public class MotionUtil implements Wrapper {
    /**
     * Checks if the local player is moving
     * @return true if the local player is moving, false if it is not
     */
    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f;
    }

    /**
     * Calculates the x and z velocity motion values
     * @param speed The speed multiplier
     * @return A two element array with the x and z values
     */
    public static double[] strafe(double speed) {
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

    /**
     * Get the needed movement values for calculating x and z motion values
     * @return A three element array containing the forward, strafe, and yaw values
     */
    public static float[] getMovement() {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
            } else if (strafe < 0.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
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
}
