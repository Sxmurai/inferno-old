package cope.inferno.util.world;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RaytraceUtil implements Wrapper {
    /**
     * Checks if we can see an entity
     * @param entity The entity to check
     * @return true if we can see it, false if we cannot
     */
    public static boolean canSeeEntity(EntityLivingBase entity) {
        return mc.player.canEntityBeSeen(entity);
    }

    /**
     * Check if we can see a block
     * @param pos The block position
     * @param boost The y boost
     * @return true if we can see it, false if we cannot
     */
    public static boolean canSeeBlock(BlockPos pos, double boost) {
        return mc.world.rayTraceBlocks(
                new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                new Vec3d(pos.getX() + 0.5, pos.getY() + boost, pos.getZ() + 0.5),
                false, // stopOnLiquid
                true, // ignoreBlockWithoutBoundingBox
                false // returnLastUncollidableBlock
        ) == null;
    }
}
