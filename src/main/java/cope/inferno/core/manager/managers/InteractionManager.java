package cope.inferno.core.manager.managers;

import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.network.NetworkUtil;
import cope.inferno.util.world.block.BlockUtil;
import cope.inferno.util.world.block.Place;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class InteractionManager implements Wrapper {
    /**
     * Places a block
     * @param pos The position to place at
     * @param place The mode for the placement
     * @param rotate If to rotate
     * @param swing If to swing your arm when placing
     */
    public void place(BlockPos pos, Place place, boolean rotate, boolean swing) {
        EnumFacing facing = BlockUtil.getFacing(pos);
        if (facing == null) {
            return;
        }

        BlockPos neighbor = pos.offset(facing);

        boolean sneak = BlockUtil.needsToSneak(neighbor);
        if (sneak) {
            LocalPlayerUtil.sneak(true);
        }

        if (rotate) {
            // @todo
        }

        Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.5));

        if (place.equals(Place.PACKET)) {
            NetworkUtil.send(new CPacketPlayerTryUseItemOnBlock(neighbor, facing.getOpposite(), mc.player.getActiveHand(), (float) (hitVec.x - pos.getX()), (float) (hitVec.y - pos.getY()), (float) (hitVec.z - pos.getZ())));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, facing.getOpposite(), hitVec, mc.player.getActiveHand());
        }

        if (swing) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }

        if (sneak) {
            LocalPlayerUtil.sneak(false);
        }
    }
}
