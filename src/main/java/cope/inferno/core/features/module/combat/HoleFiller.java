package cope.inferno.core.features.module.combat;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.manager.managers.hole.impl.Hole;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.world.block.Place;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HoleFiller extends Module {
    public HoleFiller() {
        super("HoleFiller", Category.COMBAT, "Fills in holes with a block");
    }

    public static final Setting<Type> block = new Setting<>("Block", Type.OBSIDIAN);

    public static final Setting<Swap> swap = new Setting<>("Swap", Swap.CLIENT);

    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);

    public static final Setting<Place> place = new Setting<>("Place", Place.VANILLA);
    public static final Setting<Integer> blocks = new Setting<>(place, "Blocks", 1, 1, 6);
    public static final Setting<Integer> delay = new Setting<>(place, "Delay", 1, 0, 10);

    private final Queue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();

    private int oldSlot = -1;
    private EnumHand hand = EnumHand.MAIN_HAND;

    @Override
    protected void onDisable() {
        positions.clear();

        if (nullCheck()) {
            switchBack();
        }
    }

    @Override
    public void onUpdate() {
        ArrayList<Hole> holes = getInferno().getHoleManager().getHoles();
        if (!holes.isEmpty()) {
            System.out.println("holes available");
            holes.forEach((hole) -> {
                if (hole.intersects()) {
                    return;
                }

                if (!positions.contains(hole.getPos())) {
                    positions.add(hole.getPos());
                }
            });
        }

        if (!positions.isEmpty()) {
            int slot = InventoryUtil.getHotbarBlock(block.getValue().block, null, true);
            if (slot == -1) {
                toggle();
                return;
            }

            hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

            if (!InventoryUtil.isHolding(block.getValue().block, true)) {
                if (!swap.getValue().equals(Swap.NONE)) {
                    oldSlot = mc.player.inventory.currentItem;
                    getInferno().getInventoryManager().swap(slot, swap.getValue());
                }
            }

            mc.player.setActiveHand(hand);

            if (timer.passed(delay.getValue(), Format.TICKS)) {
                for (int i = 0; i < blocks.getValue(); ++i) {
                    BlockPos pos = positions.poll();
                    if (pos == null) {
                        break;
                    }

                    getInferno().getInteractionManager().place(pos, place.getValue(), rotate.getValue(), swing.getValue());
                }
            }
        } else {
            switchBack();
        }
    }

    private void switchBack() {
        if (oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, swap.getValue());
            oldSlot = -1;
        }
    }

    public enum Type {
        OBSIDIAN(Blocks.OBSIDIAN),
        WEB(Blocks.WEB);

        private final Block block;

        Type(Block block) {
            this.block = block;
        }
    }
}
