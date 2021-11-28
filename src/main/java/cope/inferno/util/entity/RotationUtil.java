package cope.inferno.util.entity;

import cope.inferno.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Util {
    public static Rotation calcRotations(Vec3d from, Vec3d to) {
        double[] difference = new double[] { to.x - from.x, (to.y - from.y) * -1.0, to.z - from.z };
        double distance = MathHelper.sqrt(difference[0] * difference[0] + difference[2] * difference[2]);

        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difference[2], difference[0])) - 90.0f);
        float pitch = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difference[1], distance)));

        return new Rotation(yaw, pitch);
    }

    public static Rotation calcRotations(Vec3d to) {
        return RotationUtil.calcRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), to);
    }

    public static class Rotation {
        private float yaw;
        private float pitch;

        public Rotation() {
            this(-1.0f, -1.0f);
        }

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public void set(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public boolean isValid() {
            return this.yaw != -1.0f && this.pitch != -1.0f;
        }
    }
}
