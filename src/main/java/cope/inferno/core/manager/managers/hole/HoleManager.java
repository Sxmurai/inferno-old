package cope.inferno.core.manager.managers.hole;

import cope.inferno.core.manager.managers.hole.impl.DoubleHole;
import cope.inferno.core.manager.managers.hole.impl.Hole;
import cope.inferno.core.manager.managers.hole.impl.Type;
import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.internal.Wrapper;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.world.block.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class HoleManager implements Wrapper {
    private final ArrayList<Hole> holes = new ArrayList<>();
    private final Timer timer = new Timer();

    public void onUpdate() {
        if (timer.passed(200L, Format.MS)) {
            timer.reset();
            holes.clear();

            lookup:
            for (BlockPos pos : BlockUtil.getSphere(LocalPlayerUtil.getLocalPosition(), 8)) {
                if (!mc.world.isAirBlock(pos) || !BlockUtil.isHeadspaceOpen(pos, 2)) {
                    return;
                }

                if (pos.getY() == 0) {
                    holes.add(new Hole(Type.VOID, pos, false));
                } else {
                    Type type = Type.SINGLE;
                    int safety = 0;

                    for (EnumFacing facing : EnumFacing.values()) {
                        if (facing.equals(EnumFacing.UP)) {
                            continue;
                        }

                        BlockPos expansion = pos.offset(facing);
                        Block block = BlockUtil.getBlockFromPos(expansion);

                        if (BlockUtil.EXPLOSION_RESISTANT_BLOCKS.contains(block)) {
                            if (block.equals(Blocks.BEDROCK)) {
                                ++safety;
                            }
                        } else if (block.equals(Blocks.AIR)) {

                        } else {
                            continue lookup;
                        }
                    }

                    holes.add(new Hole(type, pos, safety == 5));
                }
            }
        }
    }

    public boolean isInHole() {
        return holes.stream().anyMatch((hole) -> {
            AxisAlignedBB box = new AxisAlignedBB(hole.getPos());
            if (hole instanceof DoubleHole) {
                box = ((DoubleHole) hole).getBox();
            }

            return box.intersects(mc.player.getEntityBoundingBox());
        });
    }

    public ArrayList<Hole> getHoles() {
        return holes;
    }
}
