package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.world.block.BlockUtil;
import cope.inferno.util.world.block.Place;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", Category.PLAYER, "Places blocks under you");
    }

    // swapping
    public static final Setting<ScaffoldSwap> swap = new Setting<>("Swap", ScaffoldSwap.NORMAL);
    public static final Setting<Boolean> offhand = new Setting<>(swap, "Offhand", true);
    public static final Setting<Float> swapDelay = new Setting<>(swap, "SwapDelay", 0.1f, 0.0f, 2.5f);
    public static final Setting<Boolean> noGravityBlocks = new Setting<>(swap, "NoGravityBlocks", true);

    // towering
    public static final Setting<Boolean> tower = new Setting<>("Tower", true);
    public static final Setting<Float> towerReset = new Setting<>(tower, "TowerReset", 1.5f, 0.0f, 5.0f);
    public static final Setting<Double> multiplier = new Setting<>(tower, "Multiplier", 0.3, 0.1, 1.0);

    // block placements
    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);

    public static final Setting<Place> place = new Setting<>("Place", Place.VANILLA);
    public static final Setting<Float> placeDelay = new Setting<>(place, "PlaceDelay", 25.0f, 0.0f, 2000.0f);
    public static final Setting<Boolean> stopSprint = new Setting<>(place, "StopSprint", false);

    private final Timer placeTimer = new Timer();
    private final Timer swapTimer = new Timer();
    private final Timer towerTimer = new Timer();

    private int oldSlot = -1;
    private EnumHand hand;

    @Override
    protected void onDisable() {
        hand = null;
        swapBack();
    }

    @Override
    public void onUpdate() {
        BlockPos below = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
        if (BlockUtil.isClickable(below)) {
            EnumFacing direction = BlockUtil.getFacing(below);
            if (direction == null) {
                return;
            }

            if (!handleSwapping()) {
                return;
            }

            if (placeTimer.passedMs(placeDelay.getValue().longValue())) {
                placeTimer.reset();

                if (stopSprint.getValue()) {
                    LocalPlayerUtil.sprint(false);
                }

                getInferno().getInteractionManager().place(below, place.getValue(), rotate.getValue(), swing.getValue());

                if (!BlockUtil.isReplaceable(below)) {
                    if (tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown() && direction.equals(EnumFacing.DOWN)) {
                        mc.player.motionX *= multiplier.getValue();
                        mc.player.motionZ *= multiplier.getValue();
                        mc.player.jump();

                        if (towerTimer.passedMs((long) (towerReset.getValue() * 1000.0f))) {
                            towerTimer.reset();
                            mc.player.motionY = -0.28;
                        }
                    }

                    if (!swap.getValue().equals(ScaffoldSwap.KEEP)) {
                        swapBack();
                    }
                }
            }
        }
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, swap.getValue().swap);
            oldSlot = -1;
        }

        hand = null;
    }

    private boolean handleSwapping() {
        int slot = InventoryUtil.getHotbarItem(ItemBlock.class, (item) -> {
            if (noGravityBlocks.getValue()) {
                Block block = ((ItemBlock) item).getBlock();
                return block != Blocks.SAND && block != Blocks.GRAVEL;
            }

            return true;
        }, offhand.getValue());

        if (slot == -1) {
            hand = null;
            return false;
        }

        hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        if (!InventoryUtil.isHolding(ItemBlock.class, offhand.getValue())) {
            if (!swap.getValue().equals(ScaffoldSwap.NONE)) {
                oldSlot = mc.player.inventory.currentItem;
                getInferno().getInventoryManager().swap(slot, swap.getValue().swap);

                swapTimer.reset();
            }
        } else {
            if (!swapTimer.passedMs((long) (swapDelay.getValue() * 1000.0f))) {
                return false;
            }
        }

        mc.player.setActiveHand(hand);

        return true;
    }

    public enum ScaffoldSwap {
        /**
         * Do not swap, rely on the local player to swap
         */
        NONE(Swap.NONE),

        /**
         * Normal scaffold switch
         */
        NORMAL(Swap.CLIENT),

        /**
         * Normal scaffold switch, but it's silent
         */
        PACKET(Swap.PACKET),

        /**
         * Useful on strict servers, keeps your slot on the block slot
         */
        KEEP(Swap.CLIENT);

        private final Swap swap;

        ScaffoldSwap(Swap swap) {
            this.swap = swap;
        }
    }
}
