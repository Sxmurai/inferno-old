package me.sxmurai.inferno.util.entity;

import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EntityUtil implements Wrapper {
    private static final Frustum frustum = new Frustum();

    public static List<Entity> getEntities(double range, double raytrace, boolean frustumCheck, Predicate<Entity> predicate) {
        ArrayList<Entity> entities = new ArrayList<>();
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == null || entity == mc.player || (frustumCheck && !isInFrustum(entity))) {
                continue;
            }

            double dist = mc.player.getDistance(entity);
            if (dist > range || !mc.player.canEntityBeSeen(entity) && dist > raytrace) {
                continue;
            }

            if (predicate.test(entity)) {
                entities.add(entity);
            }
        }

        return entities;
    }

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
