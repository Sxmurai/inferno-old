package cope.inferno.util.entity;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.entity.EntityLivingBase;

public class EntityUtil implements Wrapper {
    public static float getHealth(EntityLivingBase entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }
}
