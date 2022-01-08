package cope.inferno.core.features.module.player;

import cope.inferno.core.events.DamageBlockEvent;
import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.network.NetworkUtil;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.world.block.BlockUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Speedmine extends Module {
    public Speedmine() {
        super("Speedmine", Category.PLAYER, "Mines things faster");
    }

    public static final Setting<Swap> swap = new Setting<>("Swap", Swap.PACKET);
    public static final Setting<Tool> tool = new Setting<>(swap, "Tool", Tool.BEST);

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET);
    public static final Setting<Destroy> destroy = new Setting<>(mode, "Destroy", Destroy.AUTOMATIC);

    public static final Setting<Boolean> strict = new Setting<>("Strict", false);
    public static final Setting<Boolean> reset = new Setting<>("Reset", true);

    public static final Setting<Swing> swing = new Setting<>("Swing", Swing.ALLOW);
    public static final Setting<Boolean> onlyMainCancel = new Setting<>(swing, "OnlyMainCancel", true);

    public static final Setting<Boolean> render = new Setting<>("Render", true);
    public static final Setting<Boolean> filled = new Setting<>(render, "Filled", true);
    public static final Setting<Boolean> outlined = new Setting<>(render, "Outlined", true);
    public static final Setting<Float> lineWidth = new Setting<>(outlined, "LineWidth", 3.0f, 0.1f, 5.0f);

    private BlockPos miningPos;
    private final Timer timer = new Timer();

    private int oldSlot = -1;

    @Override
    public void onRender3D() {
        if (miningPos != null && render.getValue()) {
            int red, green;
            if (allowBreak()) {
                green = 255;
                red = 0;
            } else {
                red = 255;
                green = 0;
            }

            AxisAlignedBB box = RenderUtil.getRenderBoundingBox(new AxisAlignedBB(miningPos));
            RenderUtil.renderBlockEsp(box, filled.getValue(), outlined.getValue(), lineWidth.getValue(), ColorUtil.getColor(red, green, 0, 80));
        }
    }

    @Override
    protected void onDisable() {
        if (nullCheck() && oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, swap.getValue());
            oldSlot = -1;
        }
    }

    @Override
    public void onUpdate() {
        if (miningPos != null && BlockUtil.isReplaceable(miningPos)) {
            miningPos = null;
        }

        if (miningPos == null && oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, swap.getValue());
            oldSlot = -1;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (swing.getValue().equals(Swing.CANCEL) && event.getPacket() instanceof CPacketAnimation) {
            if (onlyMainCancel.getValue()) {
                CPacketAnimation packet = event.getPacket();
                if (packet.getHand().equals(EnumHand.MAIN_HAND)) {
                    event.setCanceled(true);
                }
            } else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {
        if (destroy.getValue().equals(Destroy.CLICK) &&
                miningPos != null &&
                !BlockUtil.isReplaceable(miningPos) &&
                allowBreak()) {

            EnumFacing facing = strict.getValue() ? event.getFacing().getOpposite() : event.getFacing();
            NetworkUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, miningPos, facing));

            miningPos = null;
            return;
        }

        if (event.getPos().equals(miningPos)) {
            return;
        }

        if (reset.getValue()) {
            mc.player.stopActiveHand();
        }

        miningPos = event.getPos();

        // get the block state
        IBlockState state = mc.world.getBlockState(miningPos);

        // check if we can break the block by checking the hardness
        // this will prevent us from breaking blocks that are unbreakable in survival
        // eg. command blocks, bedrock, etc
        if (state.getBlockHardness(mc.world, miningPos) == -1) {
           miningPos = null;
           return;
        }

        // reset timer for calculation
        timer.reset();

        // swap
        if (!swap.getValue().equals(Swap.NONE)) {
            swapToBest(mc.world.getBlockState(miningPos));
        }

        if (swing.getValue().equals(Swing.ALLOW)) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        } else if (swing.getValue().equals(Swing.PACKET)) {
            NetworkUtil.send(new CPacketAnimation(EnumHand.MAIN_HAND));
        }

        switch (mode.getValue()) {
            case PACKET:
            case INSTANT: {
                sendMinePacket(miningPos, event.getFacing());
                if (mode.getValue().equals(Mode.INSTANT)) {
                    mc.world.setBlockToAir(miningPos);
                }
                break;
            }

            case FASTDAMAGE: {
                mc.playerController.onPlayerDamageBlock(miningPos, event.getFacing());
                break;
            }
        }
    }

    private void swapToBest(IBlockState state) {
        double maxSpeed = 0.0;
        int slot = -1;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemTool)) {
                continue;
            }

            double breakSpeed = getBreakSpeed(stack, state);

            if ((tool.getValue().equals(Tool.PICKAXE) && stack.getItem() instanceof ItemPickaxe) ||
                    tool.getValue().equals(Tool.BEST)) {

                if (maxSpeed == 0.0) {
                    maxSpeed = breakSpeed;
                    slot = i;
                } else {
                    if (breakSpeed > maxSpeed) {
                        maxSpeed = breakSpeed;
                        slot = i;
                    }
                }
            }
        }

        if (slot == -1) {
            return;
        }

        oldSlot = mc.player.inventory.currentItem;
        getInferno().getInventoryManager().swap(slot, swap.getValue());
    }

    private double getBreakSpeed(ItemStack stack, IBlockState state) {
        double speed = stack.getDestroySpeed(state);

        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        if (efficiency > 0) {
            speed += Math.pow(efficiency, 2.0) + 1.0;
        }

        return speed;
    }

    private void sendMinePacket(BlockPos pos, EnumFacing facing) {
        if (strict.getValue()) {
            facing = facing.getOpposite();
        }

        NetworkUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, facing));

        if (destroy.getValue().equals(Destroy.AUTOMATIC)) {
            NetworkUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, facing));
        }
    }

    private boolean allowBreak() {
        if (miningPos != null) {
            IBlockState state = mc.world.getBlockState(miningPos);

            // i think this is in ticks?
            double speed = getBreakSpeed(mc.player.inventory.getStackInSlot(getInferno().getInventoryManager().getServerSlot()), state);
            return timer.passed((long) speed, Format.SECONDS);
        }

        return false;
    }

    public enum Mode {
        /**
         * Mines with packets
         */
        PACKET,

        /**
         * Makes block breaking instant
         */
        INSTANT,

        /**
         * Damages the block faster
         */
        FASTDAMAGE
    }

    public enum Swing {
        /**
         * Allow swinging
         */
        ALLOW,

        /**
         * Cancel swing packets
         */
        CANCEL,

        /**
         * Sends a packet swing
         */
        PACKET,

        /**
         * Does not swing
         */
        NONE
    }

    public enum Tool {
        /**
         * Switches to the best tool for the block
         * Eg spade on sand & gravel, axe on wood, pick on obby etc
         */
        BEST,

        /**
         * Only equip a pickaxe
         */
        PICKAXE
    }

    public enum Destroy {
        /**
         * Automatically sends the destroy packet for you
         */
        AUTOMATIC,

        /**
         * Only sends the destroy packet when you click on it
         */
        CLICK
    }
}
