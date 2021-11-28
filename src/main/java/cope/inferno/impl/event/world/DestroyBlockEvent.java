package cope.inferno.impl.event.world;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DestroyBlockEvent extends Event {
    private final BlockPos pos;

    public DestroyBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
