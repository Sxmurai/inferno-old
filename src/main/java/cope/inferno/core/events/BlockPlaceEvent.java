package cope.inferno.core.events;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class BlockPlaceEvent extends Event {
    private final BlockPos pos;
    private final EnumFacing facing;
    private final EnumHand hand;
    private final Vec3d hitVec;

    public BlockPlaceEvent(BlockPos pos, EnumFacing facing, EnumHand hand, Vec3d hitVec) {
        this.pos = pos;
        this.facing = facing;
        this.hand = hand;
        this.hitVec = hitVec;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public EnumHand getHand() {
        return hand;
    }

    public Vec3d getHitVec() {
        return hitVec;
    }
}
