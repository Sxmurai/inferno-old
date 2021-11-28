package cope.inferno.impl.event.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EntityRemoveEvent extends Event {
    private final Entity entity;

    public EntityRemoveEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
