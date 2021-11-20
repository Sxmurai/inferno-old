package me.sxmurai.inferno.util.world;

import com.google.common.collect.Lists;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil implements Wrapper {
    private static List<Block> needToShiftBlocks = Lists.newArrayList(Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BED, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.CHEST, Blocks.TRAPDOOR, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX);

    public static void place(BlockPos pos, EnumHand hand, boolean packet, boolean sneak, boolean swing, boolean rotate) {
        EnumFacing facing = BlockUtil.getFacing(pos);
        if (facing == null) {
            return;
        }

        BlockPos neighbor = pos.offset(facing);

        boolean shouldSneak = sneak || needToShiftBlocks.contains(mc.world.getBlockState(neighbor).getBlock());
        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }

        if (rotate) {
            Inferno.rotationManager.look(neighbor);
        }

        Vec3d hitVec = new Vec3d(neighbor.x + 0.5, neighbor.y + 0.5, neighbor.z + 0.5).add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.5));

        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(neighbor, facing.getOpposite(), hand, (float) (hitVec.x - pos.x), (float) (hitVec.y - pos.y), (float) (hitVec.z - pos.z)));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, facing.getOpposite(), hitVec, hand);
        }

        if (swing) {
            mc.player.swingArm(hand);
        }

        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public static EnumFacing getFacing(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (BlockUtil.isClickable(neighbor)) {
                return facing;
            }
        }

        return null;
    }

    public static boolean isClickable(BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);
        return !state.getMaterial().isReplaceable() && state.getBlock().canCollideCheck(state, false) && !BlockUtil.intersects(pos);
    }

    public static boolean intersects(BlockPos pos) {
        return !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos), (v) -> v != null && !v.isDead).isEmpty();
    }

    public static boolean isInLiquid() {
        return mc.player.isInWater() || mc.player.isInLava();
    }

    public static ArrayList<BlockPos> getSphere(BlockPos pos, int radius, int height, boolean hollow, boolean sphere, int yOffset) {
        final ArrayList<BlockPos> blocks = new ArrayList<>();
        float cx = pos.getX(), cy = pos.getY(), cz = pos.getZ(), x = cx - radius;

        while (x <= cx + radius) {
            float z = cz - radius;
            while (z <= cz + radius) {
                float y = sphere ? cy - radius : cy;
                while (true) {
                    float f = y;
                    float f2 = sphere ? cy + radius : (cy + height);
                    if (!(f < f2)) {
                        break;
                    }

                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (!(!(dist < Math.pow(radius, 2)) || hollow && dist < ((radius - 1f) * (radius - 1f)))) {
                        blocks.add(new BlockPos(x, y + yOffset, z));
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }

        return blocks;
    }

    public static boolean canSeePos(BlockPos pos, double offset) {
        offset = offset == -1.0 ? 0.0 : offset;
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.x + 0.5, pos.y + offset, pos.z + 0.5), false, true, false) == null;
    }
}
