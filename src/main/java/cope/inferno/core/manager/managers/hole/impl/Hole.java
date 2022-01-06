package cope.inferno.core.manager.managers.hole.impl;

import cope.inferno.util.world.block.BlockUtil;
import net.minecraft.util.math.BlockPos;

public class Hole {
    private final Type type;
    private final BlockPos pos;
    private final boolean safe;

    public Hole(Type type, BlockPos pos, boolean safe) {
        this.type = type;
        this.pos = pos;
        this.safe = safe;
    }

    public Type getType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean intersects() {
        return BlockUtil.intersects(pos);
    }
}
