package cope.inferno.util.entity;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityMob;

public class EntityUtil implements Wrapper {
    public static float getHealth(EntityLivingBase entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static boolean isInvisible(Entity entity) {
        return entity.isInvisibleToPlayer(mc.player);
    }

    public static boolean isPassive(Entity entity) {
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getRevengeTarget() == mc.player) {
            return false;
        }

        return entity.isCreatureType(EnumCreatureType.WATER_CREATURE, false) ||
                entity.isCreatureType(EnumCreatureType.AMBIENT, false) ||
                entity.isCreatureType(EnumCreatureType.CREATURE, false);
    }

    public static boolean isHostile(Entity entity) {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) || entity instanceof EntityMob;
    }
}
