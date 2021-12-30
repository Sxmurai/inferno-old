package cope.inferno.util.entity.player.rotation;

/**
 * Represents a server rotation
 */
public class Rotation {
    /**
     * Represents the yaw and pitch rotation values
     */
    private float yaw, pitch;

    /**
     * Sets both yaw and pitch to invalid values, marked as not valid
     */
    public Rotation() {
        this(Float.NaN, Float.NaN);
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

    /**
     * Sets both the yaw and pitch values to NaN
     */
    public void reset() {
        yaw = Float.NaN;
        pitch = Float.NaN;
    }

    /**
     * Checks if the rotation is valid
     * @return If both the yaw and pitch values are not NaN
     */
    public boolean isValid() {
        return !Float.isNaN(yaw) && !Float.isNaN(pitch);
    }
}
