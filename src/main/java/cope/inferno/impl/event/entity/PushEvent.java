package cope.inferno.impl.event.entity;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PushEvent extends Event {
    private final Type type;
    private final net.minecraft.entity.Entity entity;

    public PushEvent(Type type, net.minecraft.entity.Entity entity) {
        this.type = type;
        this.entity = entity;
    }

    public Type getMaterial() {
        return type;
    }

    public net.minecraft.entity.Entity getEntity() {
        return entity;
    }

    public static class Entity extends PushEvent {
        private double x, y, z;
        private boolean airborne;

        public Entity(net.minecraft.entity.Entity entity, double x, double y, double z, boolean airborne) {
            super(Type.ENTITY, entity);

            this.x = x;
            this.y = y;
            this.z = z;
            this.airborne = airborne;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public boolean isAirborne() {
            return airborne;
        }

        public void setAirborne(boolean airborne) {
            this.airborne = airborne;
        }
    }

    public enum Type {
        BLOCKS, LIQUID, ENTITY
    }
}
