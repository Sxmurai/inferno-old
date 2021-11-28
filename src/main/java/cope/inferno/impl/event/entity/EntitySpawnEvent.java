package cope.inferno.impl.event.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EntitySpawnEvent extends Event {
    private final Entity entity;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
