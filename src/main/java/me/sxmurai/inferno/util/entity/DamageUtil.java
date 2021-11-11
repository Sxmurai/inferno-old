package me.sxmurai.inferno.util.entity;

import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

import java.util.List;
import java.util.stream.Collectors;

public class DamageUtil implements Wrapper {
    public static void place(BlockPos pos, EnumHand hand, EnumFacing facing, boolean swing, double raytrace) {
        raytrace = raytrace == -1.0 ? 0.0 : raytrace;
        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.x + 0.5, pos.y + raytrace, pos.z + 0.5), false, true, false);
        EnumFacing direction = result == null || result.sideHit == null ? facing : result.sideHit;

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, 0.0f, 0.0f, 0.0f));

        if (swing) {
            mc.player.swingArm(hand);
        }
    }

    public static List<BlockPos> getPositions(int range, boolean protocol) {
        return BlockUtil.getSphere(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ), range, range, false, true, 0)
                .stream()
                .filter(pos -> canPlaceCrystal(pos, protocol))
                .collect(Collectors.toList());
    }

    public static boolean canPlaceCrystal(BlockPos pos, boolean protocol) {
        try {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!(block == Blocks.BEDROCK || block == Blocks.OBSIDIAN)) {
                return false;
            }

            BlockPos above = pos.add(0.0, 1.0, 0.0);
            BlockPos reassurance = pos.add(0.0, 2.0, 0.0);

            if ((!protocol && !mc.world.isAirBlock(reassurance)) || !mc.world.isAirBlock(above)) {
                return false;
            }

            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(above))) {
                if (entity.isDead || entity instanceof EntityEnderCrystal) {
                    continue;
                }

                return false;
            }

            if (!protocol) {
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(reassurance))) {
                    if (entity.isDead || entity instanceof EntityEnderCrystal) {
                        continue;
                    }

                    return false;
                }
            }
        } catch (Exception exception) {
            return false;
        }

        return true;
    }

    public static float calculateDamage(Vec3d vec, EntityPlayer target) {
        if (target == mc.player && mc.player.isCreative()) {
            return 0.0f;
        }

        float doublePower = 12.0f;
        double distancedSize = target.getDistanceSq(vec.x, vec.y, vec.z) / doublePower;
        double blast = target.world.getBlockDensity(vec, target.getEntityBoundingBox());

        double impact = (1.0 - distancedSize) * blast;
        float damage = (float) ((impact * impact + impact) / 2.0f * 7.0f * doublePower + 1.0);

        return getBlastReduction(target, getDamageMultiplier(damage), new Explosion(target.world, target, vec.x, vec.y, vec.z, 6.0f, false, true));
    }

    public static float getBlastReduction(EntityPlayer target, float damage, Explosion explosion) {
        DamageSource src = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, target.getTotalArmorValue(), (float) target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        int enchantModifier = 0;
        try {
            enchantModifier = EnchantmentHelper.getEnchantmentModifierDamage(target.getArmorInventoryList(), src);
        } catch (NullPointerException ignored) { }

        float eof = MathHelper.clamp(enchantModifier, 0.0f, 20.0f);
        damage *= 1.0f - eof / 25.0f;

        if (target.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4.0f;
        }

        return Math.max(0.0f, damage);
    }

    public static float getDamageMultiplier(float damage) {
        int difficulty = mc.world.getDifficulty().getId();
        return damage * (difficulty == 0 ? 0 : (difficulty == 2 ? 1 : (difficulty == 1 ? 0.5f : 1.5f)));
    }

    // i think? this is probably wrong, but im not gonna skid this so
    public static float getAttackDamage(EntityLivingBase target) {
        float damage = (float) mc.player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        float enchantModifier = EnchantmentHelper.getModifierForCreature(mc.player.getHeldItemMainhand(), target.getCreatureAttribute());

        float attackStrength = mc.player.getCooledAttackStrength(0.5f);
        damage *= 0.2f + attackStrength * attackStrength * 0.8f;

        if (mc.player.isPotionActive(MobEffects.STRENGTH)) {

        }

        return enchantModifier * damage;
    }
}
