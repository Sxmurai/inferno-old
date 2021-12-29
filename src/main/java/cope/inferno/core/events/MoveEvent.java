package cope.inferno.core.events;

import net.minecraft.entity.MoverType;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class MoveEvent extends Event {
    private final MoverType moverType;
    private double x, y, z;

    private boolean stillMove = true;

    public MoveEvent(MoverType moverType, double x, double y, double z) {
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MoverType getMoverType() {
        return moverType;
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

    public void setCanceled(boolean cancel, boolean stillMove) {
        super.setCanceled(cancel);
        this.stillMove = stillMove;
    }

    public boolean stillMove() {
        return stillMove;
    }
}