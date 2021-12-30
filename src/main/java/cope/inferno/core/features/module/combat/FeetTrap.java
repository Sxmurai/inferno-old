package cope.inferno.core.features.module.combat;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.entity.player.MotionUtil;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.world.block.BlockUtil;
import cope.inferno.util.world.block.Place;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeetTrap extends Module {
    private static final BlockPos[] OFFSETS = new BlockPos[] {
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1)
    };

    public FeetTrap() {
        super("FeetTrap", Category.COMBAT, "Traps your feet with obsidian");
    }

    public static final Setting<Swap> swap = new Setting<>("Swap", Swap.CLIENT);

    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);

    public static final Setting<Place> place = new Setting<>("Place", Place.VANILLA);
    public static final Setting<Integer> blocks = new Setting<>(place, "Blocks", 3, 1, 6);
    public static final Setting<Integer> delay = new Setting<>(place, "Delay", 1, 0, 10);

    public static final Setting<Disable> disable = new Setting<>("Disable", Disable.OFFGROUND);
    public static final Setting<Boolean> center = new Setting<>("Center", true);

    private final Queue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private final Timer placeTimer = new Timer();

    private int oldSlot = -1;
    private EnumHand hand;

    @Override
    protected void onDisable() {
        if (oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, swap.getValue());
            oldSlot = -1;
        }

        hand = null;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange) {
            SPacketBlockChange packet = event.getPacket();

            if (getSurroundingBlocks(LocalPlayerUtil.getLocalPosition()).contains(packet.getBlockPosition())) {
                place(packet.getBlockPosition());
            }
        }
    }

    @Override
    public void onUpdate() {
        if (disable.getValue().equals(Disable.OFFGROUND) && !mc.player.onGround) {
            toggle();
            return;
        }

        if (!positions.isEmpty()) {
            if (!placeTimer.passed(delay.getValue().longValue(), Format.TICKS)) {
                return;
            }

            placeTimer.reset();

            int slot = InventoryUtil.getHotbarBlock(Blocks.OBSIDIAN, null, true);
            if (slot == -1) {
                toggle(); // no obsidian found rip
                return;
            }

            hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

            if (!InventoryUtil.isHolding(Blocks.OBSIDIAN, true)) {
                if (!swap.getValue().equals(Swap.NONE)) {
                    oldSlot = mc.player.inventory.currentItem;
                    getInferno().getInventoryManager().swap(slot, swap.getValue());
                }
            }

            mc.player.setActiveHand(hand);

            if (center.getValue()) {
                MotionUtil.center();

                // recalculate
                positions.clear();
                positions.addAll(getSurroundingBlocks(LocalPlayerUtil.getLocalPosition()));
            }

            for (int i = 0; i < blocks.getValue(); ++i) {
                BlockPos pos = positions.poll();
                if (pos == null) {
                    break;
                }

                place(pos);
            }
        } else {
            positions.addAll(getSurroundingBlocks(LocalPlayerUtil.getLocalPosition()));

            if (positions.isEmpty() && disable.getValue().equals(Disable.FINISHED)) {
                toggle();
            }
        }
    }

    private void place(BlockPos pos) {
        getInferno().getInteractionManager().place(pos, place.getValue(), rotate.getValue(), swing.getValue());
    }

    private ArrayList<BlockPos> getSurroundingBlocks(BlockPos origin) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        BlockPos extendOffset = null;
        for (BlockPos pos : OFFSETS) {
             BlockPos neighbor = origin.add(pos);
             if (BlockUtil.isClickable(neighbor)) {
                 blocks.add(neighbor);
             } else {
                 // add an exception for the bottom block, shouldnt be a priority
                 if (pos.getY() == -1) {
                     continue;
                 }

                 if (BlockUtil.intersects(neighbor)) {
                     extendOffset = pos;
                 }
             }
        }

        if (extendOffset != null) {
            BlockPos next = origin.add(extendOffset);
            for (BlockPos pos : OFFSETS) {
                BlockPos neighbor = next.add(pos);
                if (BlockUtil.isClickable(neighbor)) {
                    blocks.add(neighbor);
                }
            }
        }

        return blocks;
    }

    public enum Disable {
        /**
         * Requires the user to turn off FeetTrap to stop
         */
        MANUAL,

        /**
         * Toggles the module when mc.player.onGround is false
         */
        OFFGROUND,

        /**
         * Toggle upon all blocks placed
         */
        FINISHED
    }
}
