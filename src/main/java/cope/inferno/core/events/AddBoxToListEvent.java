package cope.inferno.core.events;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

@Cancelable
public class AddBoxToListEvent extends Event {
    private final Block block;
    private final List<AxisAlignedBB> list;
    private final BlockPos pos;
    private final Entity entity;
    private AxisAlignedBB box;

    public AddBoxToListEvent(Block block, List<AxisAlignedBB> list, BlockPos pos, Entity entity, AxisAlignedBB box) {
        this.block = block;
        this.list = list;
        this.pos = pos;
        this.entity = entity;
        this.box = box;
    }

    public Block getBlock() {
        return block;
    }

    public List<AxisAlignedBB> getList() {
        return list;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Entity getEntity() {
        return entity;
    }

    public AxisAlignedBB getBox() {
        return box;
    }

    public void setBox(AxisAlignedBB box) {
        this.box = box;
    }
}