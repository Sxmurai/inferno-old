package cope.inferno.util.entity.player.rotation;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Wrapper {
    /**
     * Calculates rotations from two vecs
     * @param from The starting vector
     * @param to The ending vector
     * @return A Rotation object containing the yaw and pitch values for the rotation to the vectors
     */
    public static Rotation calcRotations(Vec3d from, Vec3d to) {
        double[] difference = new double[] { to.x - from.x, (to.y - from.y) * -1.0, to.z - from.z };
        double distance = MathHelper.sqrt(difference[0] * difference[0] + difference[2] * difference[2]);

        return new Rotation(
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difference[2], difference[0])) - 90.0f),
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difference[1], distance)))
        );
    }
}
