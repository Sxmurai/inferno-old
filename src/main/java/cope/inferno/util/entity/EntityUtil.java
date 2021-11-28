package cope.inferno.util.entity;

import cope.inferno.impl.features.Wrapper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

public class EntityUtil implements Wrapper {
    private static final Frustum frustum = new Frustum();

    public static boolean isInFrustum(Entity entity) {
        Entity view = mc.renderViewEntity;
        if (view == null) {
            return true;
        }

        frustum.setPosition(view.posX, view.posY, view.posZ);
        return frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static boolean isInvisible(Entity entity) {
        return entity instanceof EntityLivingBase && !((EntityLivingBase) entity).canEntityBeSeen(mc.player);
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public static boolean isHostile(Entity entity) {
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getRevengeTarget() != null) {
            return true;
        }

        return entity.isCreatureType(EnumCreatureType.MONSTER, false) || entity instanceof EntityMob;
    }

    public static boolean isPassive(Entity entity) {
        return !isHostile(entity) &&
                entity instanceof INpc ||
                entity.isCreatureType(EnumCreatureType.AMBIENT, false) ||
                entity.isCreatureType(EnumCreatureType.CREATURE, false) ||
                entity.isCreatureType(EnumCreatureType.WATER_CREATURE, false);
    }

    public static float getHealth(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            return -1.0f;
        }

        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
        return entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
    }
}
