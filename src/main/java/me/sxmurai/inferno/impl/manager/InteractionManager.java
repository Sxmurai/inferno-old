package me.sxmurai.inferno.impl.manager;

import com.google.common.collect.Lists;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.Wrapper;
import me.sxmurai.inferno.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class InteractionManager implements Wrapper {
    private List<Block> needToShiftBlocks = Lists.newArrayList(Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BED, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.CHEST, Blocks.ANVIL, Blocks.TRAPDOOR, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX);

    public void place(BlockPos pos, Placement placement, EnumHand hand, boolean rotate, boolean swing, boolean sneak) {
        EnumFacing facing = BlockUtil.getFacing(pos);
        if (facing == null) {
            return;
        }

        BlockPos neighbor = pos.offset(facing);

        boolean shouldSneak = sneak || this.needToShiftBlocks.contains(mc.world.getBlockState(neighbor).getBlock());
        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }

        if (rotate) {
            Inferno.rotationManager.look(neighbor);
        }

        Vec3d hitVec = new Vec3d(neighbor.x, neighbor.y, neighbor.z).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.5));

        if (placement == Placement.Packet) {
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

    public void releaseItem() {
        if (mc.player.isHandActive()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, mc.player.getPosition(), mc.player.getHorizontalFacing()));
            mc.player.stopActiveHand();
        }
    }

    public void attack(Entity entity, boolean packet, boolean rotate, boolean swing) {
        if (entity == null || entity == mc.player || !entity.canBeAttackedWithItem()) {
            return;
        }

        if (rotate) {
            Inferno.rotationManager.look(entity);
        }

        if (packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            mc.player.resetCooldown();
        } else {
            mc.playerController.attackEntity(mc.player, entity);
        }

        if (swing) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    public enum Placement {
        Legit, Packet
    }
}
