package cope.inferno.core.manager.managers.hole.impl;

import cope.inferno.util.world.block.BlockUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class DoubleHole extends Hole {
    private final AxisAlignedBB box;

    public DoubleHole(Type type, BlockPos pos, boolean safe, EnumFacing offset) {
        super(type, pos, safe);
        box = new AxisAlignedBB(pos).expand(offset.getXOffset(), 0.0, offset.getZOffset());
    }

    public AxisAlignedBB getBox() {
        return box;
    }

    @Override
    public boolean intersects() {
        return BlockUtil.intersects(box);
    }
}
