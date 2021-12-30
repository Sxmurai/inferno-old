package cope.inferno.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EntityVelocityEvent extends Event {
    private final Material material;

    public EntityVelocityEvent(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public enum Material {
        LIQUID, BLOCK, ENTITY
    }
}
