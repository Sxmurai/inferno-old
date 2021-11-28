package cope.inferno.impl.manager;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.util.timing.TickTimer;
import cope.inferno.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class HoleManager implements Wrapper {
    private final ArrayList<Hole> holes = new ArrayList<>();
    private final TickTimer timer = new TickTimer();

    public void onUpdate() {
        if (this.timer.passed(2)) {
            this.holes.clear();
            this.timer.reset();

            for (BlockPos blockPos : BlockUtil.getSphere(mc.player.getPosition(), 5, 5, false, true, 0)) {
                if (!mc.world.isAirBlock(blockPos)) {
                    continue;
                }

                int safe = 0, unsafe = 0;
                for (EnumFacing facing : EnumFacing.values()) {
                    if (facing == EnumFacing.UP) {
                        continue;
                    }

                    Block block = mc.world.getBlockState(blockPos.offset(facing)).getBlock();
                    if (block == Blocks.OBSIDIAN) {
                        ++unsafe;
                    } else if (block == Blocks.BEDROCK) {
                        ++safe;
                    }
                }

                if (safe + unsafe != 5) {
                    continue;
                }

                this.holes.add(new Hole(safe == 5 ? Rating.Safe : Rating.Unsafe, blockPos));
            }
        }
    }

    public boolean isInHole() {
        return this.holes.stream().anyMatch((hole) -> new AxisAlignedBB(hole.getPos()).intersects(mc.player.getEntityBoundingBox()));
    }

    public ArrayList<Hole> getHoles() {
        return holes;
    }

    public static class Hole {
        private final Rating rating;
        private final BlockPos pos;

        public Hole(Rating rating, BlockPos pos) {
            this.pos = pos;
            this.rating = rating;
        }

        public Rating getRating() {
            return rating;
        }

        public BlockPos getPos() {
            return pos;
        }
    }

    public enum Rating {
        Safe, Unsafe
    }
}
