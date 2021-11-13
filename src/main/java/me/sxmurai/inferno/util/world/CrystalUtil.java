package me.sxmurai.inferno.util.world;

import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class CrystalUtil implements Wrapper {
    public static void place(BlockPos pos, EnumHand hand, Placement placement, boolean swing, double boost) {
        EnumFacing facing = EnumFacing.UP;

        if (placement == Placement.Strict) {
            if (pos.getY() > mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()) {
                RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0f), new Vec3d(pos.x + 0.5, pos.y + (boost == -1.0 ? 0.5 : boost), pos.z + 0.5));
                facing = (result == null || result.sideHit == null) ? EnumFacing.DOWN : result.sideHit;
            }
        }

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0f, 0.0f, 0.0f));

        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }

    public static void destroy(EntityEnderCrystal crystal, EnumHand hand, boolean swing, boolean packet) {
        mc.player.resetCooldown();

        if (packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        } else {
            mc.playerController.attackEntity(mc.player, crystal);
        }

        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }

    public enum Placement {
        Normal, Strict
    }
}